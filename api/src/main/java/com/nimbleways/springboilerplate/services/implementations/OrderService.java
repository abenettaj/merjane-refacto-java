package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.exceptions.EntityNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductService productService;

    @Transactional
    public Order processOrder(Long orderId) {
        log.info("Processing order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(() -> {
            log.error("Order with ID {} not found", orderId);
            return new EntityNotFoundException("Order not found");
        });

        log.debug("Order {} found with {} items", orderId, order.getItems().size());
        productService.handleProducts(order.getItems());

        log.info("Order {} processing completed", orderId);
        return order;
    }

}
