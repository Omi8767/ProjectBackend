package com.project.backend.service;

import com.project.backend.DTO.OrderItemDTO;
import com.project.backend.DTO.OrderRequestDTO;
import com.project.backend.entity.*;
import com.project.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final StripeService stripeService;
    private final EmailService emailService;

    public OrderService(OrderRepository orderRepository, PaymentRepository paymentRepository, ProductRepository productRepository, CartRepository cartRepository, CustomerRepository customerRepository, StripeService stripeService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.customerRepository = customerRepository;
        this.stripeService = stripeService;
        this.emailService = emailService;
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
            oi.setImageUrl(product.getImages().get(0).getImageUrl());


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

        double netAmount = total +gst - discount;
        payment.setNetAmount(netAmount);

        payment.setStatus("PENDING");

        paymentRepository.save(payment);

        return new ResponseEntity<>(savedOrder,HttpStatus.CREATED);

    }

    public ResponseEntity<?> getOrderByCustomer(Long customerId){
        List<Order> byCustomerId = orderRepository.findByCustomer_Id(customerId);
        return ResponseEntity.ok(byCustomerId);
    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    public ResponseEntity<?> updateStatus(Long id,String status){
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = order.getPayment();
        if(status.equalsIgnoreCase("Delivered")&& payment.getPaymentMethod().equalsIgnoreCase("COD")){
            order.setStatus(status);
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);
        }

        order.setStatus(status);
        Order save = orderRepository.save(order);
        return ResponseEntity.ok(save);


    }

    @Transactional
    public Order cancelOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (
                order.getStatus().equals("DISPATCH") ||
                        order.getStatus().equals("DELIVERED")
        ) {
            throw new RuntimeException("Order cannot be cancelled");
        }

        Payment payment = paymentRepository.findByOrder_Id(id).orElse(null);

        // restore stock
        for (OrderItem item : order.getItems()) {
            Product p = productRepository.findById(item.getProductId())
                    .orElseThrow();

            p.setStock(p.getStock() + item.getQuantity());

            productRepository.save(p);
        }

        String email = order.getCustomer().getEmail();
        String subject;
        String body;

        // CARD REFUND
        if (
                payment != null &&
                        "SUCCESS".equals(payment.getStatus()) &&
                        "Card".equalsIgnoreCase(payment.getPaymentMethod())
        ) {

            try {
                stripeService.refundPayment(payment.getTransactionRef());
                payment.setStatus("REFUNDED");
                paymentRepository.save(payment);

            } catch (Exception e) {
                throw new RuntimeException("Refund failed");
            }

            subject = "Order Cancelled & Refund Initiated";
            body =
                    "Hello " + order.getCustomer().getName() + ",\n\n" +
                            "Your order #" + order.getId() + " has been cancelled.\n\n" +
                            " Since you paid via Card, your refund has been initiated.\n" +
                            "It will be credited within 2-3 business days.\n\n" +
                            "Thank you for shopping with us.";

            emailService.sendSimpleEmail(email, subject, body);
        }

//  COD PAYMENT
        if (
                payment != null &&
                        "COD".equalsIgnoreCase(payment.getPaymentMethod())
        ) {

            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);

            subject = "Order Cancelled";
            body =
                    "Hello " + order.getCustomer().getName() + ",\n\n" +
                            "Your order #" + order.getId() + " has been cancelled successfully.\n\n" +
                            " Since this was Cash on Delivery, no payment was charged.\n\n" +
                            "We hope to serve you again.";

            emailService.sendSimpleEmail(email, subject, body);
        }

        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }

    public Map<String, Object> getDashboard(){
        List<Order> orders = orderRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();

        Map<String,Object> data = new HashMap<>();

        data.put("totalOrders",orders.size());

        data.put("totalCustomers",customers.size());

        double revenue= payments.stream()
                .filter(p->"SUCCESS".equals(p.getStatus()))
                .mapToDouble(Payment::getNetAmount)
                .sum();

        data.put("totalRevenue",revenue);

        long pendingOrders = orders.stream()
                .filter(o->o.getStatus().equals("IN_PROCESS"))
                .count();

        data.put("pendingOrders",pendingOrders);

        data.put("inProcess",orders.stream()
                .filter(o->o.getStatus().equals("IN_PROCESS"))
                .count());

        data.put("confirm",orders.stream()
                .filter(o->o.getStatus().equals("CONFIRMED"))
                .count());

        data.put("dispatch",orders.stream()
                .filter(o->o.getStatus().equals("DISPATCH"))
                .count());

        data.put("delivered",orders.stream()
                .filter(o->o.getStatus().equals("DELIVERED"))
                .count());


        data.put("rejected",orders.stream()
                .filter(o->o.getStatus().equals("REJECT"))
                .count());

        data.put("cancelled",orders.stream()
                .filter(o->o.getStatus().equals("CANCELLED"))
                .count());

        data.put("paid",payments.stream()
                .filter(p->p.getStatus().equals("SUCCESS"))
                .count());

        data.put("pendingPayments",payments.stream()
                .filter(p->p.getStatus().equals("PENDING"))
                .count());

        data.put("refundPayments",payments.stream()
                .filter(p->p.getStatus().equals("REFUNDED"))
                .count());

        return data;

    }
}
