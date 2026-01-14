package com.grocery.grocery_price_minimizer.controller;

import com.grocery.grocery_price_minimizer.model.Store;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/store-products")
public class StoreProductController {

    private final JdbcTemplate jdbc;

    public StoreProductController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Get all store options for a product
     */
    @GetMapping("/product/{productId}")
    public List<Map<String, Object>> getStoreOptions(@PathVariable Long productId) {
        return jdbc.queryForList(
                """
                SELECT id, product_id, store, title, url, price_cents, last_verified, created_at
                FROM store_products
                WHERE product_id = ?
                ORDER BY price_cents ASC
                """,
                productId
        );
    }

    /**
     * Add store product listing
     */
    @PostMapping
    public Map<String, Object> addStoreProduct(@RequestBody Map<String, Object> body) {
        Long productId = ((Number) body.get("product_id")).longValue();
        String storeStr = (String) body.get("store");
        String title = (String) body.get("title");
        String url = (String) body.get("url");

        Store store = Store.fromString(storeStr);

        Integer priceCents = null;
        if (body.containsKey("price_cents") && body.get("price_cents") != null) {
            priceCents = ((Number) body.get("price_cents")).intValue();
        }

        jdbc.update(
                """
                INSERT INTO store_products (product_id, store, title, url, price_cents)
                VALUES (?, ?, ?, ?, ?)
                """,
                productId, store.getDisplayName(), title, url, priceCents
        );

        return Map.of("status", "saved");
    }

    /**
     * Update price for store product
     */
    @PutMapping("/{id}/price")
    public Map<String, Object> updatePrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Integer priceCents = ((Number) body.get("price_cents")).intValue();

        jdbc.update(
                "UPDATE store_products SET price_cents = ?, last_verified = NOW() WHERE id = ?",
                priceCents, id
        );

        return Map.of("status", "updated", "id", id, "price_cents", priceCents);
    }

    /**
     * Delete store product
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteStoreProduct(@PathVariable Long id) {
        int deleted = jdbc.update("DELETE FROM store_products WHERE id = ?", id);
        return Map.of("status", "deleted", "rows", deleted);
    }
}