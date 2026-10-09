package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.CreateOrderRequest;
import org.example.model.Order;
import org.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /orders - Creating order for customer: {}, items: {}, total: {}",
                request.getCustomerName(), request.getItems(), request.getTotalAmount());

        Order created = orderService.createOrder(request);

        log.info("Order created successfully with id: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("GET /orders - Fetching all orders");

        List<Order> orders = orderService.getAllOrders();

        log.info("Returning {} orders", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("GET /orders/{} - Fetching order", id);

        return orderService.getOrderById(id)
                .map(order -> {
                    log.info("Order {} found", id);
                    return ResponseEntity.ok(order);
                })
                .orElseGet(() -> {
                    log.warn("Order {} not found", id);
                    return ResponseEntity.notFound().build();
                });
    }
}