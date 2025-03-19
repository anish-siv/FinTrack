package com.finance.expensetracker.controller;

import com.finance.expensetracker.dto.ExpenseDTO;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.model.User;
import com.finance.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private ExpenseService expenseService;

    // Home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Successful login redirect
    @GetMapping("/login-success")
    public String loginSuccess() {
        return "redirect:/expenses";
    }

    // Registration page
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Expenses list
    @GetMapping("/expenses")
    public String listExpenses(Model model) {
        List<Expense> expenses = expenseService.getAllExpenses();
        return prepareExpensesModel(model, expenses);
    }
    
    // Filter expenses
    @GetMapping("/expenses/filter")
    public String filterExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {
        
        List<Expense> expenses = expenseService.getAllExpenses();
        boolean hasFilters = false;
        
        // Filter by category
        if (category != null && !category.isEmpty()) {
            expenses = expenses.stream()
                    .filter(expense -> category.equals(expense.getCategory()))
                    .collect(Collectors.toList());
            hasFilters = true;
        }
        
        // Filter by from date
        if (dateFrom != null) {
            LocalDateTime fromDateTime = LocalDateTime.of(dateFrom, LocalTime.MIN);
            expenses = expenses.stream()
                    .filter(expense -> expense.getExpenseDate().isEqual(fromDateTime) || expense.getExpenseDate().isAfter(fromDateTime))
                    .collect(Collectors.toList());
            hasFilters = true;
        }
        
        // Filter by to date
        if (dateTo != null) {
            LocalDateTime toDateTime = LocalDateTime.of(dateTo, LocalTime.MAX);
            expenses = expenses.stream()
                    .filter(expense -> expense.getExpenseDate().isEqual(toDateTime) || expense.getExpenseDate().isBefore(toDateTime))
                    .collect(Collectors.toList());
            hasFilters = true;
        }
        
        if (hasFilters) {
            model.addAttribute("filterMessage", "Showing " + expenses.size() + " filtered expenses");
        }
        
        return prepareExpensesModel(model, expenses);
    }
    
    // Helper method to prepare the model with expenses and calculations
    private String prepareExpensesModel(Model model, List<Expense> expenses) {
        model.addAttribute("expenses", expenses);
        
        // Calculating totals
        if (!expenses.isEmpty()) {
            // Total of all expenses
            BigDecimal totalAmount = new BigDecimal(0);
            
            // This month's expenses
            BigDecimal monthlyAmount = new BigDecimal(0);
            
            // Last 7 days expenses
            BigDecimal weeklyAmount = new BigDecimal(0);
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);
            int currentMonth = now.getMonthValue();
            int currentYear = now.getYear();
            
            for (Expense expense : expenses) {
                BigDecimal amount = expense.getAmount();
                totalAmount = totalAmount.add(amount);
                
                LocalDateTime expenseDate = expense.getExpenseDate();
                
                // Check if expense is from current month
                if (expenseDate.getMonthValue() == currentMonth && 
                    expenseDate.getYear() == currentYear) {
                    monthlyAmount = monthlyAmount.add(amount);
                }
                
                // Check if expense is from last 7 days
                if (expenseDate.isAfter(weekAgo)) {
                    weeklyAmount = weeklyAmount.add(amount);
                }
            }
            
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("monthlyAmount", monthlyAmount);
            model.addAttribute("weeklyAmount", weeklyAmount);
        }
        
        return "expenses/list";
    }

    // Add expense form
    @GetMapping("/expenses/add")
    public String addExpenseForm(Model model) {
        model.addAttribute("expense", new ExpenseDTO());
        return "expenses/add";
    }

    // Process add expense
    @PostMapping("/expenses/add")
    public String addExpense(@Valid @ModelAttribute("expense") ExpenseDTO expense, 
                            BindingResult result) {
        if (result.hasErrors()) {
            return "expenses/add";
        }
        expenseService.createExpense(expense);
        return "redirect:/expenses";
    }

    // Edit expense form
    @GetMapping("/expenses/edit/{id}")
    public String editExpenseForm(@PathVariable Long id, Model model) {
        Expense expense = expenseService.getExpenseById(id);
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setCategory(expense.getCategory());
        expenseDTO.setExpenseDate(expense.getExpenseDate());
        
        model.addAttribute("expense", expenseDTO);
        model.addAttribute("expenseId", id);
        return "expenses/edit";
    }

    // Process edit expense
    @PostMapping("/expenses/edit/{id}")
    public String updateExpense(@PathVariable Long id, 
                                @Valid @ModelAttribute("expense") ExpenseDTO expense,
                                BindingResult result) {
        if (result.hasErrors()) {
            return "expenses/edit";
        }
        expenseService.updateExpense(id, expense);
        return "redirect:/expenses";
    }

    // Delete expense
    @GetMapping("/expenses/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/expenses";
    }
}
