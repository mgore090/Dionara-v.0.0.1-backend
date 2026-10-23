package com.dianora.backend.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt
) {
}