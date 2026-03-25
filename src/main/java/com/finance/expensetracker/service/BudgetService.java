package com.finance.expensetracker.service;

import com.finance.expensetracker.dto.BudgetDTO;
import com.finance.expensetracker.model.Budget;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.model.User;
import com.finance.expensetracker.repository.BudgetRepository;
import com.finance.expensetracker.repository.ExpenseRepository;
import com.finance.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Budget createOrUpdateBudget(BudgetDTO dto) {
        User user = getCurrentUser();
        Optional<Budget> existing = budgetRepository
                .findByUserAndCategoryAndMonthAndYear(user, dto.getCategory(), dto.getMonth(), dto.getYear());

        Budget budget = existing.orElse(new Budget());
        budget.setUser(user);
        budget.setCategory(dto.getCategory());
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());

        return budgetRepository.save(budget);
    }

    public List<Budget> getBudgetsForCurrentMonth() {
        User user = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        return budgetRepository.findByUserAndMonthAndYear(user, now.getMonthValue(), now.getYear());
    }

    public List<Map<String, Object>> getBudgetProgress() {
        User user = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);
        List<Expense> allExpenses = expenseRepository.findByUser(user);

        // Sum expenses per category for the current month
        Map<String, BigDecimal> spentByCategory = new HashMap<>();
        for (Expense e : allExpenses) {
            LocalDateTime d = e.getExpenseDate();
            if (d.getMonthValue() == month && d.getYear() == year) {
                spentByCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> progress = new ArrayList<>();
        for (Budget b : budgets) {
            Map<String, Object> entry = new LinkedHashMap<>();
            BigDecimal spent = spentByCategory.getOrDefault(b.getCategory(), BigDecimal.ZERO);
            BigDecimal limit = b.getMonthlyLimit();
            int pct = limit.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : spent.multiply(BigDecimal.valueOf(100))
                           .divide(limit, 0, RoundingMode.HALF_UP)
                           .min(BigDecimal.valueOf(100)).intValue();

            entry.put("category", b.getCategory());
            entry.put("limit", limit);
            entry.put("spent", spent);
            entry.put("percentage", pct);
            entry.put("budgetId", b.getId());
            progress.add(entry);
        }
        return progress;
    }

    public void deleteBudget(Long id) {
        User user = getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        budgetRepository.delete(budget);
    }
}
