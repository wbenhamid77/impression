package com.example.Impression.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Application Impression démarrée avec succès !";
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint fonctionnel";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/api/health")
    public String apiHealth() {
        return "API OK";
    }
}