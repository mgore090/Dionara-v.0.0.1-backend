package com.dianora.backend.auth.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
    @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$") String phone,
    @NotBlank @Pattern(regexp = "^[0-9]{6}$") String otp
) {}
