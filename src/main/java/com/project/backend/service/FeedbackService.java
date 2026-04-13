package com.project.backend.service;

import com.project.backend.entity.Feedback;
import com.project.backend.repository.FeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    public ResponseEntity<?> create(Feedback feedback){
        Feedback save = repo.save(feedback);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    public ResponseEntity<List<Feedback>> findAll(){
        List<Feedback> all = repo.findAll();
        return new ResponseEntity<>(all,HttpStatus.OK);
    }
}
