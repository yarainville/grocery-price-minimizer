package com.grocery.grocery_price_minimizer.controller;

import com.grocery.grocery_price_minimizer.service.ProductService;
import com.grocery.grocery_price_minimizer.service.CSVImportService;
import com.grocery.grocery_price_minimizer.service.StoreFinderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CSVImportService csvImportService;
    private final StoreFinderService storeFinderService;

    public ProductController(ProductService productService,
                             CSVImportService csvImportService,
                             StoreFinderService storeFinderService) {
        this.productService = productService;
        this.csvImportService = csvImportService;
        this.storeFinderService = storeFinderService;
    }

    /**
     * Search OpenFoodFacts for products
     */
    @GetMapping("/search")
    public Map<String, Object> searchProducts(@RequestParam String q) {
        List<Map<String, Object>> results = productService.searchProducts(q);
        return Map.of(
                "query", q,
                "count", results.size(),
                "products", results
        );
    }

    /**
     * Get all saved products
     */
    @GetMapping
    public List<Map<String, Object>> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Get specific product
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Map<String, Object> product = productService.getProduct(id);
        if (product == null) {
            return Map.of("error", "Product not found");
        }
        return product;
    }

    /**
     * Find a product at all stores
     */
    @GetMapping("/{id}/find-in-stores")
    public Map<String, Object> findInStores(@PathVariable Long id) {
        // Get product from database
        Map<String, Object> product = productService.getProduct(id);

        if (product == null) {
            return Map.of("error", "Product not found");
        }

        String productName = (String) product.get("product_name");
        String brand = (String) product.get("brand");
        String quantity = (String) product.get("quantity");

        System.out.println("🔎 Finding product in stores: " + productName);

        // Search all stores
        List<Map<String, Object>> storeListings = storeFinderService.findProductAtStores(
                productName, brand, quantity
        );

        return Map.of(
                "product", product,
                "storeListings", storeListings,
                "totalResults", storeListings.size()
        );
    }

    /**
     * Save product to database
     */
    @PostMapping
    public Map<String, Object> saveProduct(@RequestBody Map<String, Object> productData) {
        Long productId = productService.saveProduct(productData);
        return Map.of(
                "status", "saved",
                "productId", productId
        );
    }

    /**
     * Import milk products from CSV
     */
    @PostMapping("/import/milk")
    public Map<String, Object> importMilk() {
        int count = csvImportService.importFromTSV("milk.csv");
        return Map.of("status", "imported", "filename", "milk.csv", "count", count);
    }

    /**
     * Import eggs products from CSV
     */
    @PostMapping("/import/eggs")
    public Map<String, Object> importEggs() {
        int count = csvImportService.importFromTSV("eggs.csv");
        return Map.of("status", "imported", "filename", "eggs.csv", "count", count);
    }

    /**
     * Import all CSV files
     */
    @PostMapping("/import/all")
    public Map<String, Object> importAll() {
        csvImportService.importAll();
        return Map.of("status", "imported all files");
    }
}