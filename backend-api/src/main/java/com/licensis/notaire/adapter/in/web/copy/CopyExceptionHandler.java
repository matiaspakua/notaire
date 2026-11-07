package com.licensis.notaire.adapter.in.web.copy;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.copy.CopyConflictException;
import com.licensis.notaire.domain.copy.CopyNotFoundException;

/**
 * Maps copy domain exceptions to the HTTP shapes clients already receive from
 * {@link CopyController} (#577).
 */
@RestControllerAdvice(assignableTypes = CopyController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CopyExceptionHandler {

    @ExceptionHandler(CopyNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(CopyConflictException.class)
    ResponseEntity<Map<String, String>> conflict(CopyConflictException conflict) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", conflict.getMessage()));
    }
}
