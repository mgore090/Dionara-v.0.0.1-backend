package com.dianora.backend.auth.otp.dto;

import java.time.LocalDateTime;

public record OtpUserResponse(
    Long id, String fullName, String email, String phone,
    boolean active, boolean newUser, LocalDateTime createdAt
) {}
