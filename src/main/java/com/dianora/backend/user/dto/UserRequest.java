package com.dianora.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String phone

) {
}