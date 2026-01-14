package com.grocery.grocery_price_minimizer.model;

import java.time.LocalDateTime;

public class BasketItem {
    private Long id;
    private Long basketId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createdAt;

    // For joined queries
    private Product product;

    // Constructors
    public BasketItem() {}

    public BasketItem(Long basketId, Long productId, Integer quantity) {
        this.basketId = basketId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBasketId() { return basketId; }
    public void setBasketId(Long basketId) { this.basketId = basketId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}