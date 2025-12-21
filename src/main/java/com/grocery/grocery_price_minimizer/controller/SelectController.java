package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SelectController {

    private final JdbcTemplate jdbc;

    public SelectController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostMapping("/select")
    public Map<String, Object> select(@RequestBody Map<String, String> body) {
        String store = body.get("store");
        String title = body.get("title");
        String url = body.get("url");

        jdbc.update(
                "INSERT INTO selected_products (store, title, url) VALUES (?, ?, ?)",
                store, title, url
        );

        return Map.of("status", "saved");
    }
}
