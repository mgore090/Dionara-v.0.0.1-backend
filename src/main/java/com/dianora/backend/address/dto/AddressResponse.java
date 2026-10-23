package com.dianora.backend.address.dto;

import java.time.LocalDateTime;

public record AddressResponse(
        Long id,
        Long userId,
        String fullName,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String country,
        String postalCode,
        String addressType,
        boolean defaultAddress,
        LocalDateTime createdAt
) {
}