package com.licensis.notaire.adapter.in.web.management;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.management.ManagementArchiveConflictException;
import com.licensis.notaire.domain.management.ManagementArchiveNotFoundException;

/**
 * Maps archive domain exceptions to the HTTP shapes clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = ManagementController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManagementArchiveExceptionHandler {

    @ExceptionHandler(ManagementArchiveNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ManagementArchiveConflictException.class)
    ResponseEntity<Map<String, Object>> conflict(ManagementArchiveConflictException conflict) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", conflict.getMessage(),
                "waitingFolderNumbers", conflict.waitingFolderNumbers()));
    }
}
