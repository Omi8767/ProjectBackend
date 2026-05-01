package com.project.backend.controller;

import com.project.backend.DTO.OrderRequestDTO;
import com.project.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO dto){
        return orderService.placeOrder(dto);
    }

    @GetMapping("/{customerId}/customer")
    public ResponseEntity<?> getOrderByCustomer(@PathVariable Long customerId){
       return orderService.getOrderByCustomer(customerId);
    }
}
