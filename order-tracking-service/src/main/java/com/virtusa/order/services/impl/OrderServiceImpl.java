package com.virtusa.order.services.impl;

import com.virtusa.order.client.ProductClient;
import com.virtusa.order.dto.*;
import com.virtusa.order.dto.OrderItemRequest;
import com.virtusa.order.dto.OrderRequest;
import com.virtusa.order.dto.OrderResponse;
import com.virtusa.order.dto.ProductResponseDto;
import com.virtusa.order.entities.Order;
import com.virtusa.order.entities.OrderItem;
import com.virtusa.order.entities.OrderStatus;
import com.virtusa.order.exceptions.InsufficientStockException;
import com.virtusa.order.exceptions.InvalidOrderException;
import com.virtusa.order.exceptions.ServiceUnavailableException;
import com.virtusa.order.repositories.OrderRepository;
import com.virtusa.order.services.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;



    @Transactional
    @CircuitBreaker(name = "productService", fallbackMethod = "productFallback")
    public OrderResponse placeOrder(OrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest item : request.getItems()) {

            ProductResponseDto product =
                    productClient.getProduct(item.getProductId());

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new InvalidOrderException(
                        "Product is inactive: " + product.getName());
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Reduce stock in PRODUCT-SERVICE
            productClient.reduceStock(
                    new StockUpdateRequest(item.getProductId(), item.getQuantity())
            );

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .build();

            orderItems.add(orderItem);

            totalAmount = totalAmount.add(
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        return saveOrder(
                request.getCustomerId(),
                orderItems,
                totalAmount
        );
    }

    /* ===========================
   🔥 SAVE ORDER (REQUIRED)
   =========================== */
    private OrderResponse saveOrder(
            Long customerId,
            List<OrderItem> orderItems,
            BigDecimal totalAmount) {

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.CREATED)
                .totalAmount(totalAmount)
                .items(orderItems)
                .build();

        // Set bidirectional mapping
        orderItems.forEach(item -> item.setOrder(order));

        Order savedOrder = orderRepository.save(order);

        return buildOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new InvalidOrderException("Order not found"));

        return buildOrderResponse(order);
    }

    /* ===========================
           JAVA 8 STREAMS SUMMARY
           =========================== */
    private OrderResponse buildOrderResponse(Order order) {

        List<String> productSummary = order.getItems()
                .stream()
                .map(item ->
                        item.getProductName() + " x " + item.getQuantity())
                .toList();

        int totalItems = order.getItems()
                .stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .totalItems(totalItems)
                .productSummary(productSummary)
                .createdAt(order.getCreatedAt())
                .build();
    }

    /* ===========================
       CIRCUIT BREAKER FALLBACK
       =========================== */
    public OrderResponse productFallback(
            OrderRequest request, Throwable ex) {

        throw new ServiceUnavailableException(
                "Order service is temporarily unavailable. Please try again later.");
    }
}