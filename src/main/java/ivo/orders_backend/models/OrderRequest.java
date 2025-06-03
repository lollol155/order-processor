package ivo.orders_backend.models;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequest(
        @NotBlank
        String customerId,

        @NotEmpty
        @Size(min = 1, message = "At least one product is required")
        List<@Valid Product> products
) {}