package com.project.backend.controller;

import com.project.backend.DTO.OrderRequestDTO;
import com.project.backend.entity.Order;
import com.project.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId){
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAll(){
        List<Order> allOrders = orderService.getAllOrders();
        return ResponseEntity.ok(allOrders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrder(@PathVariable Long id,@RequestParam String status){
        return orderService.updateStatus(id,status);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String,Object>> getDashboard(){
        Map<String, Object> dashboard = orderService.getDashboard();
        return  ResponseEntity.ok(dashboard);
    }
}
