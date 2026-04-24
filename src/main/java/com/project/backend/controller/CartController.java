package com.project.backend.controller;

import com.project.backend.DTO.CartDTO;
import com.project.backend.entity.Cart;
import com.project.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody CartDTO dto){
       return cartService.add(dto);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<List<Cart>> getCart(@PathVariable Long customerId){
        return cartService.getCartByCustomer(customerId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@RequestBody CartDTO dto){
        return cartService.update(id,dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        cartService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear/customer/{id}")
    public ResponseEntity<Void> clearCart(@PathVariable Long id){
        cartService.clearCart(id);
        return ResponseEntity.ok().build();
    }
}
