package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    private final JdbcTemplate jdbc;

    public SearchController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/search")
    public List<Map<String, Object>> search(
            @RequestParam String item,
            @RequestParam(required = false) String store
    ) {
        List<Map<String, Object>> results = new ArrayList<>();

        // 1) Return DB candidates already saved for this basket item (so user sees previous choices)
        if (store == null || store.isBlank()) {
            results.addAll(jdbc.queryForList(
                    """
                    SELECT basket_item, store, title, url, price_cents, created_at
                    FROM selected_products
                    WHERE basket_item = ?
                    ORDER BY created_at DESC
                    """,
                    item
            ));
        } else {
            results.addAll(jdbc.queryForList(
                    """
                    SELECT basket_item, store, title, url, price_cents, created_at
                    FROM selected_products
                    WHERE basket_item = ? AND store = ?
                    ORDER BY created_at DESC
                    """,
                    item, store
            ));
        }

        // 2) Fake candidates (placeholder for real scraping later)
        // If store filter is provided, only return that store's fake candidate
        if (store == null || store.isBlank()) {
            results.add(Map.of("basket_item", item, "store", "Walmart", "title", "Fake " + item, "url", "https://example.com/walmart"));
            results.add(Map.of("basket_item", item, "store", "Metro", "title", "Fake " + item, "url", "https://example.com/metro"));
            results.add(Map.of("basket_item", item, "store", "IGA", "title", "Fake " + item, "url", "https://example.com/iga"));
            results.add(Map.of("basket_item", item, "store", "Maxi", "title", "Fake " + item, "url", "https://example.com/maxi"));
        } else {
            results.add(Map.of("basket_item", item, "store", store, "title", "Fake " + item, "url", "https://example.com/" + store.toLowerCase()));
        }

        return results;
    }




}
