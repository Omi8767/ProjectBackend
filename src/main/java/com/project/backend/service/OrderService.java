package com.project.backend.service;

import com.project.backend.DTO.OrderItemDTO;
import com.project.backend.DTO.OrderRequestDTO;
import com.project.backend.entity.*;
import com.project.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, ProductRepository productRepository, CartRepository cartRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public ResponseEntity<?> placeOrder(OrderRequestDTO request){
        Order order = new Order();
        Optional<Customer> byId = customerRepository.findById(request.getCustomerId());
        if(byId.isPresent()){
            Customer customer = byId.get();
            order.setCustomer(customer);
        }
        else {
            return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        }

        List<OrderItem > orderItems = new ArrayList<>();
        double total = 0.0;

        for(OrderItemDTO it:request.getItems()){
            Product product = productRepository.findById(it.getProductId()).get();

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getId());
            oi.setProductName(product.getName());
            oi.setQuantity(it.getQuantity());
            oi.setPrice(product.getPrice());
            oi.setTotal(product.getPrice() * it.getQuantity());

            orderItems.add(oi);

            product.setStock(product.getStock() - it.getQuantity());
            productRepository.save(product);

            total +=oi.getTotal();
        }

        order.setStatus("IN_PROCESS");
        order.setTotalAmount(total);

        ShippingInfo s = new ShippingInfo();
        if(request.getShipping() != null){
            s.setName(request.getShipping().getName());
            s.setAddress(request.getShipping().getAddress());
            s.setCity(request.getShipping().getCity());
            s.setContact(request.getShipping().getContact());
            s.setPinCode(request.getShipping().getPinCode());
        }

        order.setShipping(s);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);


        //payment
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setTotalAmount(total);

        double gstPercent = request.getGstPercent() != null ? request.getGstPercent() : 0;
        double discountPercent = request.getDiscountPercent() != null ? request.getDiscountPercent() : 0;

        double gst = total * gstPercent /100;
        double discount = total* discountPercent /100;

        payment.setGstPercent(gstPercent);
        payment.setDiscountPercent(discountPercent);
        payment.setGstAmount(gst);
        payment.setDiscountAmount(discount);

        payment.setStatus("PENDING");

        return new ResponseEntity<>(savedOrder,HttpStatus.CREATED);

    }
}
