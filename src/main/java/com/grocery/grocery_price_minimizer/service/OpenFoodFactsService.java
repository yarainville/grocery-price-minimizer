package com.grocery.grocery_price_minimizer.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenFoodFactsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String SEARCH_URL = "https://world.openfoodfacts.org/cgi/search.pl";

    /**
     * Search OpenFoodFacts for products available in Canada
     */
    public List<Map<String, Object>> searchProducts(String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "%s?search_terms=%s&countries_tags=en:canada&page_size=50&json=1&fields=code,product_name,brands,quantity,image_url,categories",
                    SEARCH_URL,
                    encodedQuery
            );

            System.out.println("🔍 Searching OpenFoodFacts (Canada only) for: " + query);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("products")) {
                List<Map<String, Object>> products = (List<Map<String, Object>>) response.get("products");
                System.out.println("📦 Found " + products.size() + " products from Canada");

                // Filter and clean results
                List<Map<String, Object>> cleaned = new ArrayList<>();
                for (Map<String, Object> product : products) {
                    if (isValidProduct(product)) {
                        cleaned.add(cleanProduct(product));
                    }
                }

                System.out.println("✅ Returning " + cleaned.size() + " valid Canadian products");
                return cleaned;
            }

        } catch (Exception e) {
            System.err.println("💥 Error searching OpenFoodFacts: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Check if product has minimum required fields
     */
    private boolean isValidProduct(Map<String, Object> product) {
        return product.containsKey("product_name") &&
                product.get("product_name") != null &&
                !product.get("product_name").toString().isEmpty();
    }

    /**
     * Extract only the fields we need
     */
    private Map<String, Object> cleanProduct(Map<String, Object> product) {
        return Map.of(
                "barcode", product.getOrDefault("code", ""),
                "product_name", product.getOrDefault("product_name", ""),
                "brand", product.getOrDefault("brands", ""),
                "quantity", product.getOrDefault("quantity", ""),
                "image_url", product.getOrDefault("image_url", ""),
                "categories", product.getOrDefault("categories", "")
        );
    }
}