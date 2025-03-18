package com.finance.expensetracker.model;

import java.math.BigDecimal;
import java.time.*;

import jakarta.persistence.*;
import lombok.Data;

@Data // Lombok annotation to generate getters, setters, equals, and hashcode methods
@Entity // Entity annotation to indicate that this class is a JPA entity
@Table(name = "expenses") // Table annotation to specify the name of the table in the database
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(name = "expense_date", nullable = false)
    private LocalDateTime expenseDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne (fetch = FetchType.LAZY) // Many expenses can be associated with one user; lazy loading to fetch user details only when needed
    @JoinColumn(name = "user_id", nullable = false) 
    private User user; // User entity to match the expense to a specific user

    // Automatically sets the createdAt field to the current date and time when the expense is created
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

}

