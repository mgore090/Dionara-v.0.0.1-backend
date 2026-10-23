package com.dianora.backend.notification.service;

import com.dianora.backend.notification.dto.CreateNotificationRequest;
import com.dianora.backend.notification.dto.NotificationResponse;
import com.dianora.backend.notification.entity.Notification;
import com.dianora.backend.notification.entity.NotificationChannel;
import com.dianora.backend.notification.repository.NotificationRepository;

import com.dianora.backend.order.entity.Order;
import com.dianora.backend.order.repository.OrderRepository;

import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            OrderRepository orderRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public NotificationResponse createNotification(
            CreateNotificationRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Order order = null;

        if (request.orderId() != null) {
            order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Order not found"
                    ));

            if (!order.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Order does not belong to this user"
                );
            }
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setOrder(order);
        notification.setType(request.type());
        notification.setChannel(
                request.channel() == null
                        ? NotificationChannel.IN_APP
                        : request.channel()
        );
        notification.setTitle(request.title());
        notification.setMessage(request.message());

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(
            Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            );
        }

        return notificationRepository
                .findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            );
        }

        return notificationRepository
                .countByUser_IdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long userId,
            Long notificationId) {

        Notification notification =
                notificationRepository
                        .findByIdAndUser_Id(notificationId, userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Notification not found for this user"
                        ));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        return toResponse(
                notificationRepository.save(notification)
        );
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found"
            );
        }

        List<Notification> notifications =
                notificationRepository
                        .findAllByUser_IdOrderByCreatedAtDesc(userId);

        int updatedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notification.setReadAt(now);
                updatedCount++;
            }
        }

        notificationRepository.saveAll(notifications);
        return updatedCount;
    }

    private NotificationResponse toResponse(
            Notification notification) {

        Order order = notification.getOrder();

        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                order != null ? order.getId() : null,
                order != null ? order.getOrderNumber() : null,
                notification.getType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}