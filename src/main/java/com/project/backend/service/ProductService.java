package com.project.backend.service;

import com.project.backend.DTO.ProductDTO;
import com.project.backend.DTO.SpecificationDTO;
import com.project.backend.entity.Category;
import com.project.backend.entity.Product;
import com.project.backend.entity.ProductImage;
import com.project.backend.entity.ProductSpecification;
import com.project.backend.repository.CategoryRepository;
import com.project.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repo;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository repo, CategoryRepository categoryRepository) {
        this.repo = repo;
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<?> create(ProductDTO dto){
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setAvailable(dto.getAvailable()!=null?dto.getAvailable():true);
        Optional<Category> byIdCategory = categoryRepository.findById(dto.getCategoryId());
        if(byIdCategory.isPresent()){
            Category category = byIdCategory.get();
            product.setCategory(category);
        }
        else {
            return new ResponseEntity<>("Category not found", HttpStatus.NOT_FOUND);
        }

        //productImage
        if(dto.getImageurls() != null){
            boolean isFirst = true;

            for(String imgUrl: dto.getImageurls()){
                ProductImage img = new ProductImage();
                img.setImageUrl(imgUrl);
                img.setProduct(product);
                img.setPrimary(isFirst);
                isFirst=false;
            }
        }
        //productSpecification
        if(dto.getSpecifications() != null){
            for(SpecificationDTO s:dto.getSpecifications()){
                ProductSpecification specification = new ProductSpecification();
                specification.setName(s.getName());
                specification.setValue(s.getValue());
                specification.setProduct(product);
                product.getSpecifications().add(specification);
            }
        }

        Product save = repo.save(product);

        return ResponseEntity.ok(save);
    }
}
