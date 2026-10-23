package com.dianora.backend.notification.dto;

import com.dianora.backend.notification.entity.NotificationChannel;
import com.dianora.backend.notification.entity.NotificationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateNotificationRequest(

        @NotNull
        Long userId,

        Long orderId,

        @NotNull
        NotificationType type,

        NotificationChannel channel,

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 2000)
        String message

) {}