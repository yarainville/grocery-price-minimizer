package com.grocery.grocery_price_minimizer.service;

import com.grocery.grocery_price_minimizer.model.Store;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreFinderService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String searchEngineId;

    public StoreFinderService(
            @Value("${google.search.api.key}") String apiKey,
            @Value("${google.search.engine.id}") String searchEngineId
    ) {
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Find a specific product at all stores using Google search
     */
    public List<Map<String, Object>> findProductAtStores(String productName, String brand, String quantity) {
        List<Map<String, Object>> allResults = new ArrayList<>();

        // Search each store
        for (Store store : Store.values()) {
            List<Map<String, Object>> storeResults = searchStoreForProduct(store, productName, brand, quantity);
            allResults.addAll(storeResults);
        }

        return allResults;
    }

    /**
     * Search a specific store for a product
     */
    private List<Map<String, Object>> searchStoreForProduct(Store store, String productName, String brand, String quantity) {
        List<Map<String, Object>> results = new ArrayList<>();

        // Build specific search query
        String searchQuery = buildSearchQuery(productName, brand, quantity);
        String query = String.format("site:%s %s", store.getDomain(), searchQuery);

        System.out.println("🔍 Searching " + store.getDisplayName() + " for: " + searchQuery);

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&num=5",
                    apiKey,
                    searchEngineId,
                    encodedQuery
            );

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

                System.out.println("   📦 Found " + items.size() + " results");

                for (Map<String, Object> item : items) {
                    String title = (String) item.get("title");
                    String link = (String) item.get("link");

                    // Filter out junk pages
                    if (isProductPage(link, store)) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("store", store.name());
                        result.put("title", title);
                        result.put("url", link);
                        results.add(result);
                    }
                }
            } else {
                System.out.println("   ❌ No results found");
            }

        } catch (Exception e) {
            System.err.println("   💥 Error: " + e.getMessage());
        }

        return results;
    }

    /**
     * Build search query from product details
     */
    private String buildSearchQuery(String productName, String brand, String quantity) {
        StringBuilder query = new StringBuilder();

        if (brand != null && !brand.trim().isEmpty()) {
            query.append(brand).append(" ");
        }

        if (productName != null && !productName.trim().isEmpty()) {
            query.append(productName).append(" ");
        }

        if (quantity != null && !quantity.trim().isEmpty()) {
            query.append(quantity);
        }

        return query.toString().trim();
    }

    /**
     * Check if URL looks like a product page
     */
    private boolean isProductPage(String url, Store store) {
        String lower = url.toLowerCase();

        // Exclude junk
        if (lower.contains("facebook.com") || lower.contains("twitter.com") ||
                lower.contains("instagram.com") || lower.contains("youtube.com") ||
                lower.contains("reddit.com") || lower.contains("linkedin.com") ||
                lower.contains("wikipedia.org") || lower.contains("quora.com") ||
                lower.contains("/careers") || lower.contains("/jobs") ||
                lower.contains("/about") || lower.contains("/policies") ||
                lower.contains("/forums") || lower.contains("workday") ||
                lower.endsWith(".pdf")) {
            return false;
        }

        // Store-specific product page patterns
        if (store == Store.WALMART) {
            return lower.contains("/ip/") || lower.contains("/browse/");
        }

        // For other stores, be permissive
        return true;
    }
}