//
//package com.dianora.backend.product.entity;
//
//import com.dianora.backend.category.entity.Category;
//import jakarta.persistence.*;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(
//        name = "products",
//        indexes = {
//                @Index(name = "idx_product_category", columnList = "category_id")
//        }
//)
//public class Product {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 150)
//    private String name;
//
//    @Column(nullable = false, precision = 10, scale = 2)
//    private BigDecimal price;
//
//    @Column(precision = 10, scale = 2)
//    private BigDecimal originalPrice;
//
//    @Column(length = 3000)
//    private String description;
//
//    @Column(length = 1000)
//    private String imageUrl;
//
//    @Column(length = 100)
//    private String size;
//
//    @Column(nullable = false)
//    private int stock;
//
//    @Column(nullable = false)
//    private boolean active = true;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(
//            name = "category_id",
//            nullable = false,
//            foreignKey = @ForeignKey(name = "fk_product_category")
//    )
//    private Category category;
//
//    @ElementCollection
//    @CollectionTable(
//            name = "product_images",
//            joinColumns = @JoinColumn(name = "product_id")
//    )
//    @OrderColumn(name = "image_order")
//    @Column(name = "image_url", length = 1000)
//    private List<String> images = new ArrayList<>();
//
//    @ElementCollection
//    @CollectionTable(
//            name = "product_features",
//            joinColumns = @JoinColumn(name = "product_id")
//    )
//    @OrderColumn(name = "feature_order")
//    @Column(name = "feature_text", length = 500)
//    private List<String> features = new ArrayList<>();
//
//    protected Product() {
//    }
//
//    public Product(
//            String name,
//            BigDecimal price,
//            String description,
//            String imageUrl,
//            int stock,
//            Category category
//    ) {
//        this.name = name;
//        this.price = price;
//        this.description = description;
//        this.imageUrl = imageUrl;
//        this.stock = stock;
//        this.category = category;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public BigDecimal getPrice() {
//        return price;
//    }
//
//    public BigDecimal getOriginalPrice() {
//        return originalPrice;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public String getImageUrl() {
//        return imageUrl;
//    }
//
//    public String getSize() {
//        return size;
//    }
//
//    public int getStock() {
//        return stock;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public Category getCategory() {
//        return category;
//    }
//
//    public List<String> getImages() {
//        return images;
//    }
//
//    public List<String> getFeatures() {
//        return features;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setPrice(BigDecimal price) {
//        this.price = price;
//    }
//
//    public void setOriginalPrice(BigDecimal originalPrice) {
//        this.originalPrice = originalPrice;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public void setImageUrl(String imageUrl) {
//        this.imageUrl = imageUrl;
//    }
//
//    public void setSize(String size) {
//        this.size = size;
//    }
//
//    public void setStock(int stock) {
//        this.stock = stock;
//    }
//
//    public void setCategory(Category category) {
//        this.category = category;
//    }
//
//    public void setImages(List<String> images) {
//        this.images.clear();
//
//        if (images != null) {
//            this.images.addAll(images);
//        }
//    }
//
//    public void setFeatures(List<String> features) {
//        this.features.clear();
//
//        if (features != null) {
//            this.features.addAll(features);
//        }
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//}


package com.dianora.backend.product.entity;

import com.dianora.backend.category.entity.Category;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_product_category", columnList = "category_id"))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(length = 3000)
    private String description;

    @Column(length = 1000)
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", nullable = false, length = 1000)
    @OrderColumn(name = "image_order")
    private List<String> images = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_features", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "feature", length = 500)
    @OrderColumn(name = "feature_order")
    private List<String> features = new ArrayList<>();

    @Column(length = 100)
    private String size;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    protected Product() {}

    public Product(String name, BigDecimal price, String description, String imageUrl, int stock, Category category) {
        this.name = name; this.price = price; this.description = description;
        this.imageUrl = imageUrl; this.stock = stock; this.category = category;
        if (imageUrl != null && !imageUrl.isBlank()) this.images.add(imageUrl);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public List<String> getImages() { return images; }
    public List<String> getFeatures() { return features; }
    public String getSize() { return size; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
    public Category getCategory() { return category; }
    public void setName(String v) { name = v; }
    public void setPrice(BigDecimal v) { price = v; }
    public void setOriginalPrice(BigDecimal v) { originalPrice = v; }
    public void setDescription(String v) { description = v; }
    public void setImageUrl(String v) { imageUrl = v; }
    public void setImages(List<String> v) { images = v == null ? new ArrayList<>() : new ArrayList<>(v); }
    public void setFeatures(List<String> v) { features = v == null ? new ArrayList<>() : new ArrayList<>(v); }
    public void setSize(String v) { size = v; }
    public void setStock(int v) { stock = v; }
    public void setCategory(Category v) { category = v; }
    public void setActive(boolean v) { active = v; }
}

