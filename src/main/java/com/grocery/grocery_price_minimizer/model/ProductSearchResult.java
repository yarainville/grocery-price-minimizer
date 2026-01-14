package com.grocery.grocery_price_minimizer.model;

import java.time.LocalDateTime;

public class ProductSearchResult {
    private Long id;
    private Long basketId;
    private String basketItem;
    private Store store;
    private String title;
    private String url;
    private Integer priceCents;
    private LocalDateTime createdAt;
    private boolean isSelected; // true if from DB, false if candidate

    // Constructor for database rows (selected products)
    public ProductSearchResult(Long id, Long basketId, String basketItem, Store store,
                               String title, String url, Integer priceCents,
                               LocalDateTime createdAt, boolean isSelected) {
        this.id = id;
        this.basketId = basketId;
        this.basketItem = basketItem;
        this.store = store;
        this.title = title;
        this.url = url;
        this.priceCents = priceCents;
        this.createdAt = createdAt;
        this.isSelected = isSelected;
    }

    // Constructor for candidate products (fake/search results)
    public ProductSearchResult(Long basketId, String basketItem, Store store,
                               String title, String url) {
        this.basketId = basketId;
        this.basketItem = basketItem;
        this.store = store;
        this.title = title;
        this.url = url;
        this.isSelected = false;
    }

    // Getters
    public Long getId() { return id; }
    public Long getBasketId() { return basketId; }
    public String getBasketItem() { return basketItem; }
    public Store getStore() { return store; }
    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public Integer getPriceCents() { return priceCents; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isSelected() { return isSelected; }

    // Setters (if needed)
    public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }
}