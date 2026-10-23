package com.dianora.backend.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(

        @NotBlank
        @Size(max = 100)
        String fullName,

        @NotBlank
        @Size(max = 20)
        String phone,

        @NotBlank
        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @NotBlank
        @Size(max = 100)
        String city,

        @NotBlank
        @Size(max = 100)
        String state,

        @NotBlank
        @Size(max = 100)
        String country,

        @NotBlank
        @Size(max = 10)
        String postalCode,

        @NotBlank
        @Size(max = 30)
        String addressType,

        boolean defaultAddress

) {
}