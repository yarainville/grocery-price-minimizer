package com.grocery.grocery_price_minimizer.service;

import com.grocery.grocery_price_minimizer.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final JdbcTemplate jdbc;
    private final OpenFoodFactsService openFoodFactsService;
    private final CommonTermsService commonTermsService;

    public ProductService(JdbcTemplate jdbc,
                          OpenFoodFactsService openFoodFactsService,
                          CommonTermsService commonTermsService) {
        this.jdbc = jdbc;
        this.openFoodFactsService = openFoodFactsService;
        this.commonTermsService = commonTermsService;
    }

    /**
     * Search products - database first for common terms, API fallback for others
     */
    public List<Map<String, Object>> searchProducts(String query) {
        // Check if this is a common term
        boolean isCommon = commonTermsService.isCommonTerm(query);

        if (isCommon) {
            System.out.println("🔍 Common term detected: '" + query + "' - searching DATABASE");

            // Search local database
            List<Map<String, Object>> dbResults = jdbc.queryForList(
                    """
                    SELECT id, barcode, product_name, brand, category, quantity, image_url, openfoodfacts_url, created_at
                    FROM products
                    WHERE product_name LIKE ? OR brand LIKE ? OR category LIKE ?
                    ORDER BY product_name
                    LIMIT 50
                    """,
                    "%" + query + "%", "%" + query + "%", "%" + query + "%"
            );

            System.out.println("📦 Found " + dbResults.size() + " products in database");
            return dbResults;

        } else {
            System.out.println("🌐 Non-common term: '" + query + "' - searching OpenFoodFacts API");

            // Use OpenFoodFacts API for uncommon items
            List<Map<String, Object>> apiResults = openFoodFactsService.searchProducts(query);
            System.out.println("📦 Found " + apiResults.size() + " products from API");
            return apiResults;
        }
    }

    /**
     * Save product to database (if not exists)
     * Returns product ID
     */
    public Long saveProduct(Map<String, Object> productData) {
        String barcode = (String) productData.get("barcode");

        // Check if product already exists
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM products WHERE barcode = ?", barcode
        );

        if (!existing.isEmpty()) {
            return ((Number) existing.get(0).get("id")).longValue();
        }

        // Insert new product
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO products (barcode, product_name, brand, category, quantity, image_url, openfoodfacts_url)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, (String) productData.get("barcode"));
            ps.setString(2, (String) productData.get("product_name"));
            ps.setString(3, (String) productData.get("brand"));
            ps.setString(4, (String) productData.get("categories"));
            ps.setString(5, (String) productData.get("quantity"));
            ps.setString(6, (String) productData.get("image_url"));
            ps.setString(7, (String) productData.get("openfoodfacts_url"));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    /**
     * Get product by ID
     */
    public Map<String, Object> getProduct(Long productId) {
        List<Map<String, Object>> results = jdbc.queryForList(
                "SELECT * FROM products WHERE id = ?", productId
        );

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get all products
     */
    public List<Map<String, Object>> getAllProducts() {
        return jdbc.queryForList(
                "SELECT * FROM products ORDER BY created_at DESC LIMIT 100"
        );
    }
}