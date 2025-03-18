package com.finance.expensetracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.model.User;
import java.util.*;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // The Spring Data JPA will automatically implement the methods for basic CRUD operations
    List<Expense> findByUser(User user); // Method to find expense by a user
}
