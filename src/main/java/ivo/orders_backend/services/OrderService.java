package ivo.orders_backend.services;


import ivo.orders_backend.models.OrderEntity;
import ivo.orders_backend.models.OrderRequest;
import ivo.orders_backend.models.OrderResponse;
import ivo.orders_backend.models.OrderStatus;
import ivo.orders_backend.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    @Transactional
    public OrderResponse createNewOrder(OrderRequest request) {
        log.info("Creating new order for customerId={}", request.customerId());

        final BigDecimal totalAmount = request.products().stream()
                .map(item -> item.price()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Order total calculated: {}", totalAmount);

        final OrderEntity entity = OrderEntity.builder()
                .orderId(UUID.randomUUID().toString())
                .customerId(request.customerId())
                .totalAmount(totalAmount)
                .status(OrderStatus.ACCEPTED)
                .build();

        repository.save(entity);

        log.info("Order saved with orderId={}", entity.getOrderId());

        return convertToResponse(entity);
    }

    public Optional<OrderResponse> findOrderById(String orderId) {
        return repository.findById(orderId).map(this::convertToResponse);
    }

    private OrderResponse convertToResponse(OrderEntity entity) {
        return new OrderResponse(entity.getOrderId(), entity.getCustomerId(), entity.getTotalAmount(), entity.getStatus());
    }
}