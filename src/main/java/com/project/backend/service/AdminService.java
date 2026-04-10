package com.project.backend.service;

import com.project.backend.entity.Admin;
import com.project.backend.repository.AdminRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public ResponseEntity<?> create(Admin admin){
          Optional<Admin> byUsername = adminRepository.findByUsername(admin.getUsername());
          if(byUsername.isPresent()){
              return  ResponseEntity.badRequest().body("Username already exists");
          }
          Admin save = adminRepository.save(admin);
          save.setPassword(null);
          return ResponseEntity.ok(save);
    }

    public ResponseEntity<?> login(Admin admin){
        Optional<Admin> byUsername = adminRepository.findByUsername(admin.getUsername());
        if(byUsername.isPresent()){
            Admin admin1 = byUsername.get();
            if(admin1.getPassword().equals(admin.getPassword())){
                admin1.setPassword(null);
                return  ResponseEntity.ok(admin1);
            }
            return ResponseEntity.status(401).body("Invalid username password");
        }
        return ResponseEntity.status(401).body("Invalid username password");
    }
}
