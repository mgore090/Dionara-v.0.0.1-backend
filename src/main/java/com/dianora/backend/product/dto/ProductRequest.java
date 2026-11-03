//package com.dianora.backend.product.dto;
//
//import com.fasterxml.jackson.annotation.JsonAlias;
//import jakarta.validation.constraints.*;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//public record ProductRequest(
//
//        @NotBlank(message = "Product name is required")
//        @Size(max = 150)
//        String name,
//
//        @NotNull(message = "Price is required")
//        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
//        @Digits(integer = 8, fraction = 2)
//        BigDecimal price,
//
//        @Size(max = 3000)
//        String description,
//
//        @JsonAlias("image")
//        @Size(max = 1000)
//        String imageUrl,
//
//        @NotNull(message = "Category is required")
//        @Positive(message = "Category ID must be positive")
//        Long categoryId,
//
//        @Min(value = 0, message = "Stock cannot be negative")
//        int stock,
//
//        @DecimalMin(value = "0.00", message = "Original price cannot be negative")
//        @Digits(integer = 8, fraction = 2)
//        BigDecimal originalPrice,
//
//        @Size(max = 20, message = "Maximum 20 additional images allowed")
//        List<@Size(max = 1000) String> images,
//
//        @Size(max = 50, message = "Maximum 50 features allowed")
//        List<@Size(max = 500) String> features,
//
//        @Size(max = 100)
//        String size
//
//) {
//}


package com.dianora.backend.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "Product name is required") @Size(max = 150) String name,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @Size(max = 3000) String description,
        @Size(max = 1000) String imageUrl,
        @NotNull(message = "Category is required") @Positive Long categoryId,
        @Min(value = 0) int stock,
        BigDecimal originalPrice,
        @Size(max = 10, message = "Maximum 10 product images allowed") List<@NotBlank @Size(max = 1000) String> images,
        List<@Size(max = 500) String> features,
        @Size(max = 100) String size
) {
}
