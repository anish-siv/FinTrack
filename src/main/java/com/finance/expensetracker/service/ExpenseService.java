package com.finance.expensetracker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finance.expensetracker.exception.ResourceNotFoundException;
import com.finance.expensetracker.dto.ExpenseDTO;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.repository.ExpenseRepository;

@Service // Service annotation ensures that this class is a Spring-managed bean
public class ExpenseService {

    @Autowired // Autowired annotation injects the ExpenseRepository dependency automatically
    private ExpenseRepository expenseRepository;

    // Method to create a new expense
    public Expense createExpense(ExpenseDTO expenseDTO) {
        Expense expense = new Expense(); // Creating a new Expense object
        expense.setDescription(expenseDTO.getDescription()); // Setting the description from the DTO
        expense.setAmount(expenseDTO.getAmount()); // Setting the amount from the DTO
        expense.setCategory(expenseDTO.getCategory()); // Setting the category from the DTO
        expense.setExpenseDate(expenseDTO.getExpenseDate()); // Setting the expense date from the DTO

        return expenseRepository.save(expense); // Saving the new expense to the database
    }

    // Method to get all expenses
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll(); // Retrieving all expenses from the database
    }

    // Method to get an expense by its ID
    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id) // Retrieving the expense by its ID
            .orElseThrow(() -> new ResourceNotFoundException( // Throwing an exception if the expense is not found
                "Expense not found with id: " + id));
    }

    // Method to delete an expense by its ID
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id); // Throwing an exception if the expense is not found
        }
        expenseRepository.deleteById(id); // Deleting the expense from the database
    }
}
