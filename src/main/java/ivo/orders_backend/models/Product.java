package ivo.orders_backend.models;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record Product(
        @NotBlank
        String productId,
        @NotNull
        @Min(value = 1, message = "Quantity must be greater than 0.")
        Integer quantity,
        @NotNull
        @DecimalMin(value = "0.01", message = "Price must be greater than 0.")
        BigDecimal price
) {}
