package com.finance.expensetracker.repository;

import com.finance.expensetracker.model.Budget;
import com.finance.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndMonthAndYear(User user, int month, int year);
    Optional<Budget> findByUserAndCategoryAndMonthAndYear(User user, String category, int month, int year);
}
