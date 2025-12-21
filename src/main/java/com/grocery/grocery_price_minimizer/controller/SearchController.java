package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Map<String, Object>> search(@RequestParam String item) {

        List<Map<String, Object>> results = new ArrayList<>();

        // 1) Add saved selections from the database (accurate items you already confirmed)
        results.addAll(jdbc.queryForList(
                "SELECT store, title, url, created_at FROM selected_products ORDER BY created_at DESC"
        ));

        // 2) Add fake search candidates (we will replace these with real store search later)
        results.add(Map.of("store", "Walmart", "title", "Fake " + item, "url", "https://example.com/walmart"));
        results.add(Map.of("store", "Metro", "title", "Fake " + item, "url", "https://example.com/metro"));
        results.add(Map.of("store", "IGA", "title", "Fake " + item, "url", "https://example.com/iga"));
        results.add(Map.of("store", "Maxi", "title", "Fake " + item, "url", "https://example.com/maxi"));

        return results;
    }



}
