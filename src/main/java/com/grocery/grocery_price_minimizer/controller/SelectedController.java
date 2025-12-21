package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SelectedController {

    private final JdbcTemplate jdbc;

    public SelectedController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/selected")
    public List<Map<String, Object>> getSelected() {
        return jdbc.queryForList(
                "SELECT id, store, title, url, created_at FROM selected_products ORDER BY created_at DESC"
        );
    }
}
