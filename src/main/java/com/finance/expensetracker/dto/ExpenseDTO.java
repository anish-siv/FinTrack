package com.finance.expensetracker.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ExpenseDTO {
    
    @NotBlank(message = "Description is required") // NotBlank annotation ensures that the description is not blank
    private String description;
    
    @NotNull(message = "Amount is required") // NotNull annotation ensures that the amount is not null
    @Positive(message = "Amount must be positive") // Positive annotation ensures that the amount is positive
    private BigDecimal amount;
    
    @NotBlank(message = "Category is required") // NotBlank annotation ensures that the category is not blank
    private String category;
    
    @NotNull(message = "Expense date is required") // NotNull annotation ensures that the expense date is not null
    private LocalDateTime expenseDate;
}