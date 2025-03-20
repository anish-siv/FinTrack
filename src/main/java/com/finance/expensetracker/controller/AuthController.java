package com.finance.expensetracker.controller;

import com.finance.expensetracker.model.User;
import com.finance.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController { // AuthController class to handle authentication requests
    
    private final UserService userService; // Variable to store the user service

    @PostMapping("/register") // PostMapping annotation to specify that this method handles HTTP POST requests
    public ResponseEntity<User> register(@RequestBody User user) { // Method to register a user
        return ResponseEntity.ok(userService.registerUser(user)); // Return the registered user
    }
}
