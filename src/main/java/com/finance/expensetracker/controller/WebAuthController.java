package com.finance.expensetracker.controller;

import com.finance.expensetracker.model.User;
import com.finance.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAuthController {
    
    private final UserService userService;
    
    @PostMapping("/api/auth/register-web")
    public String registerWeb(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(user);
            return "redirect:/login?registered";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
} 