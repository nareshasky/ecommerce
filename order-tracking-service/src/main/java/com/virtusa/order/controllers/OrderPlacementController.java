package com.virtusa.order.controllers;

import com.virtusa.order.dto.OrderRequest;
import com.virtusa.order.dto.OrderResponse;
import com.virtusa.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderPlacementController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Validated @RequestBody OrderRequest request) {

        return new ResponseEntity<>(
                orderService.placeOrder(request),
                HttpStatus.CREATED
        );
    }
}
