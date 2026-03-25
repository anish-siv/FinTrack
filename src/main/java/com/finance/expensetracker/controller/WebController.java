package com.finance.expensetracker.controller;

import com.finance.expensetracker.dto.BudgetDTO;
import com.finance.expensetracker.dto.ExpenseDTO;
import com.finance.expensetracker.model.Expense;
import com.finance.expensetracker.model.User;
import com.finance.expensetracker.service.AiInsightService;
import com.finance.expensetracker.service.BudgetService;
import com.finance.expensetracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private AiInsightService aiInsightService;

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

        if (category != null && !category.isEmpty()) {
            expenses = expenses.stream()
                    .filter(expense -> category.equals(expense.getCategory()))
                    .collect(Collectors.toList());
            hasFilters = true;
        }

        if (dateFrom != null) {
            LocalDateTime fromDateTime = LocalDateTime.of(dateFrom, LocalTime.MIN);
            expenses = expenses.stream()
                    .filter(expense -> expense.getExpenseDate().isEqual(fromDateTime) || expense.getExpenseDate().isAfter(fromDateTime))
                    .collect(Collectors.toList());
            hasFilters = true;
        }

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

    // Helper method to prepare the model with expenses, calculations, chart data, and budget progress
    private String prepareExpensesModel(Model model, List<Expense> expenses) {
        model.addAttribute("expenses", expenses);

        // Calculating totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal monthlyAmount = BigDecimal.ZERO;
        BigDecimal weeklyAmount = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Category totals for doughnut chart (LinkedHashMap preserves insertion order)
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            totalAmount = totalAmount.add(amount);

            LocalDateTime expenseDate = expense.getExpenseDate();

            if (expenseDate.getMonthValue() == currentMonth && expenseDate.getYear() == currentYear) {
                monthlyAmount = monthlyAmount.add(amount);
            }
            if (expenseDate.isAfter(weekAgo)) {
                weeklyAmount = weeklyAmount.add(amount);
            }

            categoryTotals.merge(expense.getCategory(), amount, BigDecimal::add);
        }

        if (!expenses.isEmpty()) {
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("monthlyAmount", monthlyAmount);
            model.addAttribute("weeklyAmount", weeklyAmount);
        }

        model.addAttribute("categoryTotals", categoryTotals);

        // Monthly totals for last 6 months (bar chart), pre-initialized with zero
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, BigDecimal> monthlyTotals = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            monthlyTotals.put(now.minusMonths(i).format(monthFmt), BigDecimal.ZERO);
        }
        for (Expense expense : expenses) {
            String key = expense.getExpenseDate().format(monthFmt);
            monthlyTotals.computeIfPresent(key, (k, v) -> v.add(expense.getAmount()));
        }
        model.addAttribute("monthlyTotals", monthlyTotals);

        // Budget progress for current month
        try {
            model.addAttribute("budgetProgress", budgetService.getBudgetProgress());
        } catch (Exception e) {
            model.addAttribute("budgetProgress", Collections.emptyList());
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

    // -------------------------------------------------------
    // Budget routes
    // -------------------------------------------------------

    @GetMapping("/expenses/budgets")
    public String budgetsPage(Model model) {
        LocalDateTime now = LocalDateTime.now();
        BudgetDTO dto = new BudgetDTO();
        dto.setMonth(now.getMonthValue());
        dto.setYear(now.getYear());
        model.addAttribute("budgetDTO", dto);
        model.addAttribute("budgetProgress", budgetService.getBudgetProgress());
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentYear", now.getYear());
        model.addAttribute("currentMonthName", now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        return "expenses/budgets";
    }

    @PostMapping("/expenses/budgets/save")
    public String saveBudget(@Valid @ModelAttribute("budgetDTO") BudgetDTO budgetDTO,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            LocalDateTime now = LocalDateTime.now();
            model.addAttribute("budgetProgress", budgetService.getBudgetProgress());
            model.addAttribute("currentMonth", now.getMonthValue());
            model.addAttribute("currentYear", now.getYear());
            model.addAttribute("currentMonthName", now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            return "expenses/budgets";
        }
        try {
            budgetService.createOrUpdateBudget(budgetDTO);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Could not save budget: " + e.getMessage());
            LocalDateTime now = LocalDateTime.now();
            model.addAttribute("budgetProgress", budgetService.getBudgetProgress());
            model.addAttribute("currentMonth", now.getMonthValue());
            model.addAttribute("currentYear", now.getYear());
            model.addAttribute("currentMonthName", now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            return "expenses/budgets";
        }
        return "redirect:/expenses/budgets";
    }

    @GetMapping("/expenses/budgets/delete/{id}")
    public String deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return "redirect:/expenses/budgets";
    }

    // -------------------------------------------------------
    // AI Insights endpoint
    // -------------------------------------------------------

    @PostMapping("/expenses/insights")
    @ResponseBody
    public Map<String, String> getInsight(@RequestParam String question) {
        List<Expense> expenses = expenseService.getAllExpenses();
        String response = aiInsightService.getInsight(question, expenses);
        return Map.of("response", response);
    }
}
