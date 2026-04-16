package com.project.backend.controller;

import com.project.backend.DTO.ProductDTO;
import com.project.backend.entity.Product;
import com.project.backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductDTO dto){
       return service.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@RequestBody ProductDTO dto){
        return service.update(id,dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
       return   service.delete(id);
    }

    @GetMapping
    public ResponseEntity<List<Product>> all(){
       return service.getAllProduct();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id){
       return service.getById(id);
    }
}
