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

    @PostMapping("/select/bulk")
    public Map<String, Object> selectBulk(@RequestBody List<Map<String, Object>> items) {
        int count = 0;

        for (Map<String, Object> body : items) {
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
            count++;
        }

        return Map.of("status", "saved", "count", count);
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

    @GetMapping("/best-store")
    public Map<String, Object> bestStore(@RequestParam String basket) {
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

        List<Map<String, Object>> chosen = jdbc.queryForList(
                """
                SELECT sp.basket_item, sp.store, sp.title, sp.url, sp.price_cents, sp.created_at
                FROM selected_products sp
                JOIN (
                    SELECT t.basket_item, MAX(t.id) AS pick_id
                    FROM selected_products t
                    WHERE t.price_cents IS NOT NULL
                      AND t.store = ?
                      AND t.basket_item IN (%s)
                      AND t.price_cents = (
                          SELECT MIN(t2.price_cents)
                          FROM selected_products t2
                          WHERE t2.store = t.store
                            AND t2.basket_item = t.basket_item
                            AND t2.price_cents IS NOT NULL
                      )
                    GROUP BY t.basket_item
                ) picks
                  ON sp.id = picks.pick_id
                ORDER BY sp.basket_item ASC
                """.formatted(placeholders),
                concatParams(store, items)
        );



        return Map.of(
                "store", store,
                "total_cents", total.intValue(),
                "items", chosen
        );
    }

    @GetMapping("/best-per-store")
    public List<Map<String, Object>> bestPerStore(@RequestParam String item) {
        return jdbc.queryForList(
                """
                SELECT sp.basket_item, sp.store, sp.title, sp.url, sp.price_cents, sp.created_at
                FROM selected_products sp
                JOIN (
                    SELECT store, MIN(price_cents) AS min_price
                    FROM selected_products
                    WHERE basket_item = ?
                      AND price_cents IS NOT NULL
                    GROUP BY store
                ) mins
                ON sp.store = mins.store AND sp.price_cents = mins.min_price
                WHERE sp.basket_item = ?
                ORDER BY sp.store ASC
                """,
                item, item
        );
    }

    @DeleteMapping("/reset")
    public Map<String, Object> reset() {
        int deleted = jdbc.update("DELETE FROM selected_products");
        return Map.of("status", "deleted", "rows", deleted);
    }

    // helper: build params = [store] + items + [store]
    private Object[] concatParams(String store, String[] items) {
        Object[] params = new Object[items.length + 1];
        params[0] = store;
        for (int i = 0; i < items.length; i++) params[i + 1] = items[i];
        return params;
    }






}
