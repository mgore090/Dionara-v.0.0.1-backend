package com.dianora.backend.returns.entity;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.entity.OrderItem;
import com.dianora.backend.user.entity.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_requests", indexes = {
        @Index(name = "idx_return_order_id", columnList = "order_id"),
        @Index(name = "idx_return_user_id", columnList = "user_id"),
        @Index(name = "idx_return_status", columnList = "status")
})
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 80)
    private String reason;

    @Column(length = 1500)
    private String additionalDetails;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private RefundMethod refundMethod;

    @Column(length = 150)
    private String refundDestination;

    @Column(length = 120)
    private String refundTransactionId;

    @Column(length = 1000)
    private String adminNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime refundedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (returnNumber == null || returnNumber.isBlank()) {
            returnNumber = "RET-" + java.util.UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12).toUpperCase();
        }
        if (status == null) status = ReturnStatus.REQUESTED;
        if (refundAmount == null) refundAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getReturnNumber() { return returnNumber; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public OrderItem getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getAdditionalDetails() { return additionalDetails; }
    public void setAdditionalDetails(String additionalDetails) { this.additionalDetails = additionalDetails; }
    public ReturnStatus getStatus() { return status; }
    public void setStatus(ReturnStatus status) { this.status = status; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public RefundMethod getRefundMethod() { return refundMethod; }
    public void setRefundMethod(RefundMethod refundMethod) { this.refundMethod = refundMethod; }
    public String getRefundDestination() { return refundDestination; }
    public void setRefundDestination(String refundDestination) { this.refundDestination = refundDestination; }
    public String getRefundTransactionId() { return refundTransactionId; }
    public void setRefundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
}
