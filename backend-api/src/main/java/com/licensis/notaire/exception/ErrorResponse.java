package com.licensis.notaire.exception;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response DTO for all API errors
 * Supports JSON serialization for consistent error handling across the API
 */
@Data
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    private Map<String, Object> details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public ErrorResponse(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message);
        this.path = path;
    }
}
