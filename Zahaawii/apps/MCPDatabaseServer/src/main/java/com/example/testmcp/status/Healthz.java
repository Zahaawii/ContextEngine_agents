package com.example.testmcp.status;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Healthz {

    @GetMapping("/healthz")
    public String healthz() {
        return "Server is up and running";
    }
}
