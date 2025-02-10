package com.finance.expensetracker.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping
    public String testEndpoint() { return "The Expense Tracker API is working!"; }
    
}
