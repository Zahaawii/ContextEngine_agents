package com.example.zahaawiiblog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/status")
public class HealthzController {

    @GetMapping("/healthz")
    public ResponseEntity<String> getStatus() {
       return new ResponseEntity<>("I am awake", HttpStatus.OK);
    }
}
