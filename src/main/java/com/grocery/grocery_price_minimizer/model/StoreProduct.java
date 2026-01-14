package com.grocery.grocery_price_minimizer.model;

import java.time.LocalDateTime;

public class StoreProduct {
    private Long id;
    private Long productId;
    private Store store;
    private String title;
    private String url;
    private Integer priceCents;
    private LocalDateTime lastVerified;
    private LocalDateTime createdAt;

    // Constructors
    public StoreProduct() {}

    public StoreProduct(Long productId, Store store, String title,
                        String url, Integer priceCents) {
        this.productId = productId;
        this.store = store;
        this.title = title;
        this.url = url;
        this.priceCents = priceCents;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getPriceCents() { return priceCents; }
    public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }

    public LocalDateTime getLastVerified() { return lastVerified; }
    public void setLastVerified(LocalDateTime lastVerified) {
        this.lastVerified = lastVerified;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}