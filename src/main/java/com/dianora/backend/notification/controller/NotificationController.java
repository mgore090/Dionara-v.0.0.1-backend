package com.dianora.backend.notification.controller;

import com.dianora.backend.notification.dto.CreateNotificationRequest;
import com.dianora.backend.notification.dto.NotificationResponse;
import com.dianora.backend.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request);
    }

    @GetMapping("/users/{userId}/notifications")
    public List<NotificationResponse> getUserNotifications(
            @PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/users/{userId}/notifications/unread-count")
    public Map<String, Long> getUnreadCount(
            @PathVariable Long userId) {
        return Map.of(
                "unreadCount",
                notificationService.getUnreadCount(userId)
        );
    }

    @PatchMapping(
            "/users/{userId}/notifications/{notificationId}/read"
    )
    public NotificationResponse markAsRead(
            @PathVariable Long userId,
            @PathVariable Long notificationId) {
        return notificationService.markAsRead(
                userId, notificationId
        );
    }

    @PatchMapping("/users/{userId}/notifications/read-all")
    public Map<String, Integer> markAllAsRead(
            @PathVariable Long userId) {
        return Map.of(
                "updatedCount",
                notificationService.markAllAsRead(userId)
        );
    }
}