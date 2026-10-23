package com.dianora.backend.coupon.entity;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.user.entity.User;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_usages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coupon_usage_order",
                        columnNames = "order_id"
                )
        },
        indexes = {
                @Index(name = "idx_coupon_usage_user",
                        columnList = "coupon_id,user_id"),
                @Index(name = "idx_coupon_usage_coupon",
                        columnList = "coupon_id")
        }
)
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false)
    private boolean released = false;

    private LocalDateTime usedAt;

    private LocalDateTime releasedAt;

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public boolean isReleased() { return released; }
    public void setReleased(boolean released) {
        this.released = released;
    }

    public LocalDateTime getUsedAt() { return usedAt; }

    public LocalDateTime getReleasedAt() { return releasedAt; }
    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
}