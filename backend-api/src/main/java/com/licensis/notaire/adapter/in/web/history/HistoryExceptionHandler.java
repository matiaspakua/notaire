package com.licensis.notaire.adapter.in.web.history;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.history.HistoryBadRequestException;
import com.licensis.notaire.domain.history.HistoryNotFoundException;

/**
 * Maps history domain exceptions to empty 400/404 bodies clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = HistoryController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HistoryExceptionHandler {

    @ExceptionHandler(HistoryBadRequestException.class)
    ResponseEntity<Void> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(HistoryNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
