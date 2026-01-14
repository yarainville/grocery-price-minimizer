package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class BasketItemController {

    private final JdbcTemplate jdbc;

    public BasketItemController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Get all items in a basket (with product details)
     */
    @GetMapping("/baskets/{basketId}/items")
    public List<Map<String, Object>> getBasketItems(@PathVariable Long basketId) {
        return jdbc.queryForList(
                """
                SELECT bi.id, bi.basket_id, bi.product_id, bi.quantity, bi.created_at,
                       p.barcode, p.product_name, p.brand, p.category, p.quantity as product_quantity,
                       p.image_url, p.openfoodfacts_url
                FROM basket_items bi
                JOIN products p ON bi.product_id = p.id
                WHERE bi.basket_id = ?
                ORDER BY bi.created_at DESC
                """,
                basketId
        );
    }

    /**
     * Add product to basket
     */
    @PostMapping("/baskets/{basketId}/items")
    public Map<String, Object> addItem(
            @PathVariable Long basketId,
            @RequestBody Map<String, Object> body
    ) {
        Long productId = ((Number) body.get("product_id")).longValue();
        Integer quantity = body.containsKey("quantity")
                ? ((Number) body.get("quantity")).intValue()
                : 1;

        // Check if already in basket
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM basket_items WHERE basket_id = ? AND product_id = ?",
                basketId, productId
        );

        if (!existing.isEmpty()) {
            // Update quantity
            jdbc.update(
                    "UPDATE basket_items SET quantity = quantity + ? WHERE basket_id = ? AND product_id = ?",
                    quantity, basketId, productId
            );
            return Map.of("status", "updated", "basketId", basketId, "productId", productId);
        }

        // Insert new
        jdbc.update(
                "INSERT INTO basket_items (basket_id, product_id, quantity) VALUES (?, ?, ?)",
                basketId, productId, quantity
        );

        return Map.of("status", "added", "basketId", basketId, "productId", productId);
    }

    /**
     * Remove item from basket
     */
    @DeleteMapping("/baskets/{basketId}/items/{itemId}")
    public Map<String, Object> removeItem(
            @PathVariable Long basketId,
            @PathVariable Long itemId
    ) {
        int deleted = jdbc.update(
                "DELETE FROM basket_items WHERE id = ? AND basket_id = ?",
                itemId, basketId
        );
        return Map.of("status", "deleted", "rows", deleted);
    }
}