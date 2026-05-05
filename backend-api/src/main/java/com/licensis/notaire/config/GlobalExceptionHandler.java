package com.licensis.notaire.config;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.observability.StructuredLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for all REST controllers
 * Provides consistent error responses with structured logging
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final StructuredLogger structuredLogger = StructuredLogger.getInstance();

    /**
     * Handle NotaireException and its subclasses
     */
    @ExceptionHandler({NotaireException.class})
    public ResponseEntity<ErrorResponse> handleNotaireException(
            NotaireException ex,
            HttpServletRequest request) {
        
        int statusCode = ex.getStatusCode();
        ErrorResponse errorResponse = new ErrorResponse(
            statusCode,
            HttpStatus.valueOf(statusCode).getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        structuredLogger.logError(
            "Notaire exception occurred",
            ex,
            Map.of(
                "statusCode", statusCode,
                "path", request.getRequestURI(),
                "method", request.getMethod()
            )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.valueOf(statusCode));
    }

    /**
     * Handle ResourceNotFoundException
     */
    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            404,
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        structuredLogger.logWarn(
            "Resource not found",
            Map.of(
                "path", request.getRequestURI(),
                "message", ex.getMessage()
            )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle BusinessValidationException
     */
    @ExceptionHandler({BusinessValidationException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidationException(
            BusinessValidationException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            400,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        structuredLogger.logWarn(
            "Business validation failed",
            Map.of(
                "path", request.getRequestURI(),
                "message", ex.getMessage()
            )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            500,
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "An unexpected error occurred",
            request.getRequestURI()
        );
        
        structuredLogger.logError(
            "Unexpected exception occurred",
            ex,
            Map.of(
                "path", request.getRequestURI(),
                "method", request.getMethod(),
                "exceptionType", ex.getClass().getSimpleName()
            )
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
