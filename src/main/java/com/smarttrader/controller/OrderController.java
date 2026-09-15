package com.smarttrader.controller;

import com.smarttrader.entity.TradingOrder;
import com.smarttrader.exception.EntityNotFoundException;
import com.smarttrader.order.OrderService;
import com.smarttrader.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public OrderController(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<List<TradingOrder>> listOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<TradingOrder> getOrder(@PathVariable UUID id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Order", id.toString()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRADER', 'ADMIN')")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        TradingOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order", id.toString()));
        boolean cancelled = orderService.cancelOrder(order.getClientOrderId());
        if (cancelled) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
