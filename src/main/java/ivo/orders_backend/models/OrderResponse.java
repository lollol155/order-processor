package ivo.orders_backend.models;

import java.math.BigDecimal;

public record OrderResponse(
        String orderId,
        String customerId,
        BigDecimal totalAmount,
        OrderStatus status
) {}