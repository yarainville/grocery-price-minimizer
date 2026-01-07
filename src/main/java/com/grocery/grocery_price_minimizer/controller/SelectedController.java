package com.grocery.grocery_price_minimizer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
                """
                SELECT id, basket_item, store, title, url, price_cents, created_at
                FROM selected_products
                ORDER BY created_at DESC
                """
        );
    }

    @DeleteMapping("/selected/{id}")
    public Map<String, Object> deleteOne(@PathVariable long id) {
        int rows = jdbc.update("DELETE FROM selected_products WHERE id = ?", id);
        return Map.of("deleted", rows);
    }


}


