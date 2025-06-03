package ivo.orders_backend.models;

public enum OrderStatus {
    ACCEPTED,
    PROCESSING,
    SHIPPED,
    DELIVERING,
    DELIVERED,
    CANCELED,
    FAILED,
    LOST,
    RETURNING,
    RETURNED,
    PENDING_PAYMENT
}