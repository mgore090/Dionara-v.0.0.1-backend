package com.dianora.backend.order.entity;

import com.dianora.backend.coupon.entity.Coupon;
import com.dianora.backend.user.entity.User;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_user_id", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_created_at", columnList = "created_at")
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PLACED;

    // ADDED FOR COUPON:
    // Product prices before any discount.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    // ADDED FOR COUPON:
    // Total discount applied to this order.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // ADDED FOR COUPON:
    // A coupon can be reused by different orders,
    // but each order stores at most one coupon.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    // Final amount payable after discount.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Shipping address snapshot:
    // preserves the delivery address as it was at checkout.
    @Column(nullable = false, length = 150)
    private String shippingFullName;

    @Column(nullable = false, length = 30)
    private String shippingPhone;

    @Column(nullable = false, length = 255)
    private String shippingAddressLine1;

    @Column(length = 255)
    private String shippingAddressLine2;

    @Column(nullable = false, length = 100)
    private String shippingCity;

    @Column(nullable = false, length = 100)
    private String shippingState;

    @Column(nullable = false, length = 100)
    private String shippingCountry;

    @Column(nullable = false, length = 20)
    private String shippingPostalCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (orderNumber == null || orderNumber.isBlank()) {
            orderNumber = "DIN-" +
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 12)
                            .toUpperCase();
        }

        if (status == null) {
            status = OrderStatus.PLACED;
        }

        if (subtotalAmount == null) {
            subtotalAmount = BigDecimal.ZERO;
        }

        if (discountAmount == null) {
            discountAmount = BigDecimal.ZERO;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    // ADDED FOR COUPON:
    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    // ADDED FOR COUPON:
    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    // ADDED FOR COUPON:
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    // ADDED FOR COUPON:
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    // ADDED FOR COUPON:
    public Coupon getCoupon() {
        return coupon;
    }

    // ADDED FOR COUPON:
    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingFullName() {
        return shippingFullName;
    }

    public void setShippingFullName(String value) {
        this.shippingFullName = value;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String value) {
        this.shippingPhone = value;
    }

    public String getShippingAddressLine1() {
        return shippingAddressLine1;
    }

    public void setShippingAddressLine1(String value) {
        this.shippingAddressLine1 = value;
    }

    public String getShippingAddressLine2() {
        return shippingAddressLine2;
    }

    public void setShippingAddressLine2(String value) {
        this.shippingAddressLine2 = value;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String value) {
        this.shippingCity = value;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String value) {
        this.shippingState = value;
    }

    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String value) {
        this.shippingCountry = value;
    }

    public String getShippingPostalCode() {
        return shippingPostalCode;
    }

    public void setShippingPostalCode(String value) {
        this.shippingPostalCode = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}