package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.services.implementations.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("{orderId}/processOrder")
    public ResponseEntity<ProcessOrderResponse> processOrder(@PathVariable Long orderId) {
        log.info("Received request to process order with ID: {}", orderId);
        Order order = orderService.processOrder(orderId);

        log.info("Order {} processed successfully", orderId);
        return ResponseEntity.ok(new ProcessOrderResponse(order.getId()));
    }
}
