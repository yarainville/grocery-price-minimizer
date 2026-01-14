package com.grocery.grocery_price_minimizer.controller;

import com.grocery.grocery_price_minimizer.model.Store;
import com.grocery.grocery_price_minimizer.service.BestStoreService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
public class SelectController {

    private final JdbcTemplate jdbc;
    private final BestStoreService bestStoreService;

    public SelectController(JdbcTemplate jdbc, BestStoreService bestStoreService) {
        this.jdbc = jdbc;
        this.bestStoreService = bestStoreService;
    }

    @PostMapping("/select")
    public Map<String, Object> select(@RequestBody Map<String, Object> body)  {
        Long basketId = ((Number) body.get("basket_id")).longValue();
        String basketItem = (String) body.get("basket_item");
        String storeStr = (String) body.get("store");
        String title = (String) body.get("title");
        String url = (String) body.get("url");

        // Validate store name
        Store store = Store.fromString(storeStr);

        Integer priceCents = null;
        Object pc = body.get("price_cents");
        if (pc instanceof Number n) {
            priceCents = n.intValue();
        }

        jdbc.update(
                "INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents) VALUES (?, ?, ?, ?, ?, ?)",
                basketId, basketItem, store.getDisplayName(), title, url, priceCents
        );

        return Map.of("status", "saved", "store", store.name());
    }

    @PostMapping("/select/bulk")
    public Map<String, Object> selectBulk(@RequestBody List<Map<String, Object>> items) {
        int count = 0;

        for (Map<String, Object> body : items) {
            String basketItem = (String) body.get("basket_item");
            String storeStr = (String) body.get("store");
            String title = (String) body.get("title");
            String url = (String) body.get("url");

            Store store = Store.fromString(storeStr);

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
                    basketItem, store.getDisplayName(), title, url, priceCents
            );
            count++;
        }

        return Map.of("status", "saved", "count", count);
    }

    @PostMapping("/baskets")
    public Map<String, Object> createBasket() {
        jdbc.update("INSERT INTO baskets () VALUES ()");
        Long newId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return Map.of("basketId", newId);
    }

    @GetMapping("/baskets")
    public List<Map<String, Object>> listBaskets() {
        return jdbc.queryForList(
                "SELECT id, created_at FROM baskets ORDER BY id DESC"
        );
    }

    @PostMapping("/seed")
    public Map<String, Object> seed(@RequestParam long basketId) {
        jdbc.update("""
        INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents)
        VALUES (?, 'milk', ?, 'Milk A', 'https://a', 499)
        """, basketId, Store.MAXI.getDisplayName());

        jdbc.update("""
        INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents)
        VALUES (?, 'milk', ?, 'Milk B', 'https://b', 459)
        """, basketId, Store.MAXI.getDisplayName());

        jdbc.update("""
        INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents)
        VALUES (?, 'milk', ?, 'Walmart Milk A', 'https://w-milk-a', 479)
        """, basketId, Store.WALMART.getDisplayName());

        jdbc.update("""
        INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents)
        VALUES (?, 'eggs', ?, 'Eggs A (12)', 'https://eggs-a', 399)
        """, basketId, Store.MAXI.getDisplayName());

        jdbc.update("""
        INSERT INTO selected_products (basket_id, basket_item, store, title, url, price_cents)
        VALUES (?, 'eggs', ?, 'Eggs Walmart A (12)', 'https://w-eggs-a', 349)
        """, basketId, Store.WALMART.getDisplayName());

        return Map.of("status", "seeded", "basketId", basketId);
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
    public Map<String, Object> bestStore(
            @RequestParam long basketId,
            @RequestParam String basket
    ) {
        String[] items = basket.split(",");
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
                    WHERE basket_id = ?
                      AND price_cents IS NOT NULL
                      AND basket_item IN (%s)
                    GROUP BY store, basket_item
                ) per_item
                GROUP BY store
                HAVING COUNT(*) = %d
                ORDER BY total_cents ASC
                LIMIT 1
                """.formatted(placeholders, items.length),
                concatParamsFirst(basketId, items)
        );

        if (rows.isEmpty()) {
            return Map.of(
                    "error", "No store has all items with prices yet",
                    "basketId", basketId,
                    "basket", basket
            );
        }

        String bestStore = (String) rows.get(0).get("store");
        Number total = (Number) rows.get(0).get("total_cents");
        int totalCents = total == null ? 0 : total.intValue();

        List<Map<String, Object>> chosen = jdbc.queryForList(
                """
                SELECT sp.basket_item, sp.store, sp.title, sp.url, sp.price_cents, sp.created_at
                FROM selected_products sp
                JOIN (
                    SELECT t.basket_item, MAX(t.id) AS pick_id
                    FROM selected_products t
                    WHERE t.basket_id = ?
                      AND t.price_cents IS NOT NULL
                      AND t.store = ?
                      AND t.basket_item IN (%s)
                      AND t.price_cents = (
                          SELECT MIN(t2.price_cents)
                          FROM selected_products t2
                          WHERE t2.basket_id = t.basket_id
                            AND t2.store = t.store
                            AND t2.basket_item = t.basket_item
                            AND t2.price_cents IS NOT NULL
                      )
                    GROUP BY t.basket_item
                ) picks
                  ON sp.id = picks.pick_id
                ORDER BY sp.basket_item ASC
                """.formatted(placeholders),
                concatParamsFirst(basketId, bestStore, items)
        );

        return Map.of(
                "basketId", basketId,
                "store", bestStore,
                "total_cents", totalCents,
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

    @DeleteMapping("/basket")
    public Map<String, Object> clearBasket(@RequestParam long basketId) {
        int deleted = jdbc.update(
                "DELETE FROM selected_products WHERE basket_id = ?",
                basketId
        );
        return Map.of("status", "cleared", "basketId", basketId, "deleted", deleted);
    }

    @DeleteMapping("/reset")
    public Map<String, Object> reset() {
        int deleted = jdbc.update("DELETE FROM selected_products");
        return Map.of("status", "deleted", "rows", deleted);
    }

    @GetMapping("/baskets/{basketId}/best-store")
    public Map<String, Object> getBestStore(@PathVariable Long basketId) {
        return bestStoreService.calculateBestStore(basketId);
    }

    private Object[] concatParamsFirst(long basketId, String[] items) {
        Object[] params = new Object[items.length + 1];
        params[0] = basketId;
        for (int i = 0; i < items.length; i++) params[i + 1] = items[i];
        return params;
    }

    private Object[] concatParamsFirst(long basketId, String store, String[] items) {
        Object[] params = new Object[items.length + 2];
        params[0] = basketId;
        params[1] = store;
        for (int i = 0; i < items.length; i++) params[i + 2] = items[i];
        return params;
    }
}