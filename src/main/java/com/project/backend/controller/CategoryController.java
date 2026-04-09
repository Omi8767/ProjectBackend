package com.project.backend.controller;

import com.project.backend.entity.Category;
import com.project.backend.entity.Enquiry;
import com.project.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> saveCategory(@RequestBody Category category){
        Category category1 = categoryService.saveCategory(category);
        return new ResponseEntity<>(category1, HttpStatus.OK);
    }

    @GetMapping
    public  ResponseEntity<List<Category>> getAllCategory(){
        List list = categoryService.getAllCategory();
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateData(@PathVariable Long id, @RequestBody Category category){
        return categoryService.updateCategory(id,category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        return categoryService.delete(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id){
        return ResponseEntity.ok(categoryService.findById(id));
    }

}
