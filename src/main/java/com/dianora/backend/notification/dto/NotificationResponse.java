package com.dianora.backend.notification.dto;

import com.dianora.backend.notification.entity.NotificationChannel;
import com.dianora.backend.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long userId,
        Long orderId,
        String orderNumber,
        NotificationType type,
        NotificationChannel channel,
        String title,
        String message,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {}