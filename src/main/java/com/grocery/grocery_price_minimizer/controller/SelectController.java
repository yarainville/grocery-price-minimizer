package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
public class SelectController {

    private final JdbcTemplate jdbc;

    public SelectController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/select")
    public Map<String, Object> select(@RequestBody Map<String, Object> body) {
        String basketItem = (String) body.get("basket_item");
        String store = (String) body.get("store");
        String title = (String) body.get("title");
        String url = (String) body.get("url");

        Integer priceCents = null;
        Object pc = body.get("price_cents");
        if (pc instanceof Number n) {
            priceCents = n.intValue();
        }

        jdbc.update(
                """
                INSERT INTO selected_products
                (basket_item, store, title, url, price_cents)
                VALUES (?, ?, ?, ?, ?)
                """,
                basketItem, store, title, url, priceCents
        );

        return Map.of("status", "saved");
    }


    @GetMapping("/cheapest")
    public List<Map<String, Object>> getCheapestPerStore() {
        return jdbc.queryForList(
        """
            SELECT sp.id, sp.store, sp.title, sp.url, sp.price_cents, sp.created_at
            FROM selected_products sp
            JOIN (
                SELECT store, MIN(price_cents) AS min_price
                FROM selected_products
                WHERE price_cents IS NOT NULL
                GROUP BY store
            ) mins
            ON sp.store = mins.store AND sp.price_cents = mins.min_price
            ORDER BY sp.store ASC
            """
        );
    }

    @GetMapping("/best-store")
    public Map<String, Object> bestStore(@RequestParam String basket) {
        // basket="milk,eggs,bread" -> ["milk","eggs","bread"]
        String[] items = basket.split(",");

        // Build placeholders (?, ?, ?) depending on basket size
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
            items[i] = items[i].trim();
        }

        List<Map<String, Object>> rows = jdbc.queryForList(
                """
                SELECT store, SUM(min_price) AS total_cents
                FROM (
                    SELECT store, basket_item, MIN(price_cents) AS min_price
                    FROM selected_products
                    WHERE price_cents IS NOT NULL
                      AND basket_item IN (%s)
                    GROUP BY store, basket_item
                ) per_item
                GROUP BY store
                HAVING COUNT(*) = %d
                ORDER BY total_cents ASC
                LIMIT 1
                """.formatted(placeholders, items.length),
                (Object[]) items
        );

        if (rows.isEmpty()) {
            return Map.of(
                    "error", "No store has all items with prices yet",
                    "basket", basket
            );
        }

        return rows.get(0);
    }


    @GetMapping("/total")
    public Map<String, Object> totalCheapestBasket() {
        Integer total = jdbc.queryForObject(
                """
                SELECT SUM(min_price) AS total_cents
                FROM (
                    SELECT store, MIN(price_cents) AS min_price
                    FROM selected_products
                    WHERE price_cents IS NOT NULL
                    GROUP BY store
                ) mins
                """,
                Integer.class
        );

        if (total == null) total = 0;

        return Map.of("total_cents", total);
    }

    @GetMapping("/best-store-details")
    public Map<String, Object> bestStoreDetails(@RequestParam String basket) {
        String[] items = basket.split(",");
        for (int i = 0; i < items.length; i++) items[i] = items[i].trim();

        // Find the best store (cheapest total) that has ALL basket items
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            if (i > 0) placeholders.append(", ");
            placeholders.append("?");
        }

        List<Map<String, Object>> best = jdbc.queryForList(
                """
                SELECT store, SUM(min_price) AS total_cents
                FROM (
                    SELECT store, basket_item, MIN(price_cents) AS min_price
                    FROM selected_products
                    WHERE price_cents IS NOT NULL
                      AND basket_item IN (%s)
                    GROUP BY store, basket_item
                ) per_item
                GROUP BY store
                HAVING COUNT(*) = %d
                ORDER BY total_cents ASC
                LIMIT 1
                """.formatted(placeholders, items.length),
                (Object[]) items
        );

        if (best.isEmpty()) {
            return Map.of("error", "No store has all items with prices yet", "basket", basket);
        }

        String store = (String) best.get(0).get("store");
        Number total = (Number) best.get(0).get("total_cents");

        // For that store, fetch the cheapest chosen product per basket_item
        List<Map<String, Object>> chosen = jdbc.queryForList(
                """
                SELECT sp.basket_item, sp.store, sp.title, sp.url, sp.price_cents, sp.created_at
                FROM selected_products sp
                JOIN (
                    SELECT basket_item, MIN(price_cents) AS min_price
                    FROM selected_products
                    WHERE price_cents IS NOT NULL
                      AND store = ?
                      AND basket_item IN (%s)
                    GROUP BY basket_item
                ) mins
                ON sp.basket_item = mins.basket_item AND sp.price_cents = mins.min_price AND sp.store = ?
                ORDER BY sp.basket_item ASC
                """.formatted(placeholders),
                concatParams(store, items, store)
        );

        return Map.of(
                "store", store,
                "total_cents", total.intValue(),
                "items", chosen
        );
    }

    // helper: build params = [store] + items + [store]
    private Object[] concatParams(String store1, String[] items, String store2) {
        Object[] params = new Object[items.length + 2];
        params[0] = store1;
        for (int i = 0; i < items.length; i++) params[i + 1] = items[i];
        params[items.length + 1] = store2;
        return params;
    }

    @DeleteMapping("/reset")
    public Map<String, Object> reset() {
        int deleted = jdbc.update("DELETE FROM selected_products");
        return Map.of("status", "deleted", "rows", deleted);
    }



}
