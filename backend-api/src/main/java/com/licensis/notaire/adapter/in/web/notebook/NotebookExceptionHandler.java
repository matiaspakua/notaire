package com.licensis.notaire.adapter.in.web.notebook;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.notebook.NotebookBadRequestException;
import com.licensis.notaire.domain.notebook.NotebookNotFoundException;

/**
 * Maps notebook create failures to the {@code {"message":…}} bodies clients already get (#577).
 */
@RestControllerAdvice(assignableTypes = NotebookController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NotebookExceptionHandler {

    @ExceptionHandler(NotebookBadRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(NotebookBadRequestException bad) {
        return ResponseEntity.badRequest().body(Map.of("message", bad.getMessage()));
    }

    @ExceptionHandler(NotebookNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(NotebookNotFoundException missing) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", missing.getMessage()));
    }
}
