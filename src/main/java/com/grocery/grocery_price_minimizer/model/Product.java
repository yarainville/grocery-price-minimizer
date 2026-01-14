package com.grocery.grocery_price_minimizer.model;

import java.time.LocalDateTime;

public class Product {
    private Long id;
    private String barcode;
    private String productName;
    private String brand;
    private String category;
    private String quantity;
    private String imageUrl;
    private String openfoodfactsUrl;
    private LocalDateTime createdAt;

    // Constructors
    public Product() {}

    public Product(String barcode, String productName, String brand,
                   String category, String quantity, String imageUrl) {
        this.barcode = barcode;
        this.productName = productName;
        this.brand = brand;
        this.category = category;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOpenfoodfactsUrl() { return openfoodfactsUrl; }
    public void setOpenfoodfactsUrl(String openfoodfactsUrl) {
        this.openfoodfactsUrl = openfoodfactsUrl;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}