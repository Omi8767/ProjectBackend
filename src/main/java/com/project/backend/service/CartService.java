package com.project.backend.service;

import com.project.backend.DTO.CartDTO;
import com.project.backend.entity.Cart;
import com.project.backend.entity.Customer;
import com.project.backend.entity.Product;
import com.project.backend.repository.CartRepository;
import com.project.backend.repository.CustomerRepository;
import com.project.backend.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, CustomerRepository customerRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public ResponseEntity<?> add(CartDTO dto){
        Cart cart = new Cart();
        Optional<Customer> byId = customerRepository.findById(dto.getCustomerId());
        if(byId.isPresent()){
            Customer customer = byId.get();
            cart.setCustomer(customer);

        }
        else {
            return ResponseEntity.notFound().build();
        }

        Optional<Product> byIdproduct = productRepository.findById(dto.getProductId());
        if(byIdproduct.isPresent()){
            Product product = byIdproduct.get();
            cart.setProduct(product);
        }
        else {
            return ResponseEntity.notFound().build();
        }

        cart.setQuantity(dto.getQuantity());

        cartRepository.save(cart);
        return  ResponseEntity.ok(cart);
    }

    public ResponseEntity<?> update(Long id,CartDTO dto){
        Optional<Cart> byId = cartRepository.findById(id);
        if(byId.isPresent()){
            Cart existing = byId.get();
            existing.setQuantity(dto.getQuantity());
            cartRepository.save(existing);
            return ResponseEntity.ok(existing);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<List<Cart>> getCartByCustomer(Long id){
        List<Cart> items = cartRepository.findByCustomer_Id(id);
        return ResponseEntity.ok(items);
    }

    public void deleteById(Long id){
        cartRepository.deleteById(id);
    }

    @Transactional
    public void clearCart(Long customerId){
        cartRepository.deleteByCustomerId(customerId);
    }


}
