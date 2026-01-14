package com.grocery.grocery_price_minimizer.service;

import com.grocery.grocery_price_minimizer.model.Store;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BestStoreService {

    private final JdbcTemplate jdbc;

    public BestStoreService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Calculate the best store for a basket
     * Returns which single store has the lowest total price
     */
    public Map<String, Object> calculateBestStore(Long basketId) {
        System.out.println("🧮 Calculating best store for basket " + basketId);

        // 1. Get all products in the basket
        List<Map<String, Object>> basketItems = jdbc.queryForList(
                """
                SELECT bi.id, bi.product_id, bi.quantity, p.product_name
                FROM basket_items bi
                JOIN products p ON bi.product_id = p.id
                WHERE bi.basket_id = ?
                """,
                basketId
        );

        if (basketItems.isEmpty()) {
            return Map.of("error", "Basket is empty");
        }

        System.out.println("📦 Basket has " + basketItems.size() + " products");

        // 2. For each store, calculate total cost
        Map<String, Integer> storeTotals = new HashMap<>();
        Map<String, List<Map<String, Object>>> storeItems = new HashMap<>();

        for (Store store : Store.values()) {
            int total = 0;
            List<Map<String, Object>> items = new ArrayList<>();
            boolean hasAllProducts = true;

            for (Map<String, Object> basketItem : basketItems) {
                Long productId = ((Number) basketItem.get("product_id")).longValue();
                Integer quantity = ((Number) basketItem.get("quantity")).intValue();

                // Find cheapest price for this product at this store
                List<Map<String, Object>> storePrices = jdbc.queryForList(
                        """
                        SELECT id, title, url, price_cents
                        FROM store_products
                        WHERE product_id = ? AND store = ? AND price_cents IS NOT NULL
                        ORDER BY price_cents ASC
                        LIMIT 1
                        """,
                        productId, store.getDisplayName()
                );

                if (storePrices.isEmpty()) {
                    // Store doesn't have this product
                    hasAllProducts = false;
                    break;
                }

                Map<String, Object> storePrice = storePrices.get(0);
                Integer priceCents = ((Number) storePrice.get("price_cents")).intValue();
                total += priceCents * quantity;

                Map<String, Object> itemDetail = new HashMap<>(basketItem);
                itemDetail.put("store_product", storePrice);
                itemDetail.put("subtotal_cents", priceCents * quantity);
                items.add(itemDetail);
            }

            if (hasAllProducts) {
                storeTotals.put(store.getDisplayName(), total);
                storeItems.put(store.getDisplayName(), items);
                System.out.println("   " + store.getDisplayName() + ": $" + (total / 100.0));
            } else {
                System.out.println("   " + store.getDisplayName() + ": Missing products");
            }
        }

        // 3. Find the store with lowest total
        if (storeTotals.isEmpty()) {
            return Map.of("error", "No store has all products with prices");
        }

        String bestStore = null;
        int lowestTotal = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : storeTotals.entrySet()) {
            if (entry.getValue() < lowestTotal) {
                lowestTotal = entry.getValue();
                bestStore = entry.getKey();
            }
        }

        System.out.println("🏆 Best store: " + bestStore + " ($" + (lowestTotal / 100.0) + ")");

        return Map.of(
                "basketId", basketId,
                "bestStore", bestStore,
                "totalCents", lowestTotal,
                "totalDollars", lowestTotal / 100.0,
                "items", storeItems.get(bestStore),
                "allStoreTotals", storeTotals
        );
    }
}