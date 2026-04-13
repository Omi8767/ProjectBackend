package com.project.backend.controller;

import com.project.backend.entity.Feedback;
import com.project.backend.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Feedback feedback){
        return service.create(feedback);
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> all(){
       return   service.findAll();
    }
}
