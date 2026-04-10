package com.project.backend.service;

import com.project.backend.entity.Category;
import com.project.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public Category saveCategory(Category category){
//        if(category.getCategoryname() == null || category.getCategoryname() =="") throw new RuntimeException("Name is empty");
        if(category.getCategoryname() != null && categoryRepository.existsByCategoryname(category.getCategoryname())){
            throw new RuntimeException("Category already exists");
        }

        return categoryRepository.save(category);
    }
    public List<Category> getAllCategory(){

        return categoryRepository.findAll();

    }

    public ResponseEntity<?> updateCategory( Long id, Category category){
        Optional<Category> byId = categoryRepository.findById(id);
        if(byId.isPresent()){
            Category category1 = byId.get();
            category1.setCategoryname(category.getCategoryname());
            category1.setImageURL(category.getImageURL());
            Category save = categoryRepository.save(category1);
            return new ResponseEntity<>(save, HttpStatus.OK);
        }
        return new ResponseEntity<>("Category Not Found",HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> delete( Long id){
        Optional<Category> byId = categoryRepository.findById(id);
        if(byId.isPresent()) {
            categoryRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Record Deleted"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Category Not Found"));
    }

    public Category findById(Long id){
        return categoryRepository.findById(id).orElseThrow(()->
                new RuntimeException("Category not found"));
    }

}
