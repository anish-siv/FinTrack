package com.finance.expensetracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(String message, int status, String error, String path) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.status = status;
        this.error = error;
        this.path = path;
    }
}
