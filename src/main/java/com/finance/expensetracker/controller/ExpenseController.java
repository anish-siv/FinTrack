package com.finance.expensetracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.finance.expensetracker.dto.ExpenseDTO;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;

@RestController // RestController annotation indicates that this class is a REST controller
@RequestMapping("/api/expenses") // RequestMapping annotation to specify the base URL for this controller
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;

    @PostMapping // PostMapping annotation to specify that this method handles HTTP POST requests
    public ResponseEntity<Expense> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO) {
        return new ResponseEntity<>(expenseService.createExpense(expenseDTO), HttpStatus.CREATED);
    }

    @GetMapping // GetMapping annotation to specify that this method handles HTTP GET requests
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}") // GetMapping annotation to specify that this method handles HTTP GET requests
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @DeleteMapping("/{id}") // DeleteMapping annotation to specify that this method handles HTTP DELETE requests
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    // All requests are tested using Postman
}
