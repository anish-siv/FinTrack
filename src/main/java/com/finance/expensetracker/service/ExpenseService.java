package com.finance.expensetracker.service;

import java.util.List;

import com.finance.expensetracker.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.finance.expensetracker.exception.ResourceNotFoundException;
import com.finance.expensetracker.dto.ExpenseDTO;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.repository.ExpenseRepository;
import com.finance.expensetracker.repository.UserRepository;

@Service // Service annotation ensures that this class is a Spring-managed bean
public class ExpenseService {

    @Autowired // Autowired annotation injects the ExpenseRepository dependency automatically
    private ExpenseRepository expenseRepository; // Injecting the ExpenseRepository dependency

    @Autowired // Autowired annotation injects the UserRepository dependency automatically
    private UserRepository userRepository; // Injecting the UserRepository dependency

    private User getCurrentUser() { // Method to get the current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Retrieving the authentication object from the security context
        
        if (authentication == null || !authentication.isAuthenticated()) { // Checking if the user is authenticated
            throw new AccessDeniedException("User not authenticated"); // Throwing an exception if the user is not authenticated
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")); // Returning the user object
    }

    // Method to create a new expense
    public Expense createExpense(ExpenseDTO expenseDTO) {
        User currentUser = getCurrentUser();
        
        Expense expense = new Expense(); // Creating a new Expense object
        expense.setDescription(expenseDTO.getDescription()); // Setting the description from the DTO
        expense.setAmount(expenseDTO.getAmount()); // Setting the amount from the DTO
        expense.setCategory(expenseDTO.getCategory()); // Setting the category from the DTO
        expense.setExpenseDate(expenseDTO.getExpenseDate()); // Setting the expense date from the DTO
        expense.setUser(currentUser); // Setting the user from the current user

        return expenseRepository.save(expense); // Saving the new expense to the database
    }

    // Method to get all expenses
    public List<Expense> getAllExpenses() {
        User currentUser = getCurrentUser(); // Getting the current user

        return expenseRepository.findByUser(currentUser); // Retrieving all expenses from the database
    }

    // Method to get an expense by its ID
    public Expense getExpenseById(Long id) {
        User currentUser = getCurrentUser();
        Expense expense = expenseRepository.findById(id) // Retrieving the expense by its ID
            .orElseThrow(() -> new ResourceNotFoundException( // Throwing an exception if the expense is not found
                "Expense not found with id: " + id));
        
        // Security check - ensure expense belongs to current user
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        
        return expense;
    }

    // Method to update an expense by its ID
    public Expense updateExpense(Long id, ExpenseDTO expenseDTO) {
        Expense expense = getExpenseById(id); // Retrieving the expense by its ID
        expense.setDescription(expenseDTO.getDescription()); // Updating the description
        expense.setAmount(expenseDTO.getAmount()); // Updating the amount
        expense.setCategory(expenseDTO.getCategory()); // Updating the category
        expense.setExpenseDate(expenseDTO.getExpenseDate()); // Updating the expense date
        
        return expenseRepository.save(expense); // Saving the updated expense to the database
    }

    // Method to delete an expense by its ID
    public void deleteExpense(Long id) {
        Expense expense = getExpenseById(id);
        expenseRepository.delete(expense);
    }
}
