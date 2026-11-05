package com.dianora.backend.auth.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendOtpRequest(
    @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$")
    String phone
) {}
