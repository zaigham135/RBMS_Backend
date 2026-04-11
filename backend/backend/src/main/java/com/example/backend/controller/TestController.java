package com.example.backend.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String testApi() {
        return "Access Granted: Secure API Working!";
    }
}