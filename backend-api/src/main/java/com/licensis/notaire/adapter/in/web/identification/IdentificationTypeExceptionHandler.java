package com.licensis.notaire.adapter.in.web.identification;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.identification.IdentificationTypeInUseException;
import com.licensis.notaire.domain.identification.IdentificationTypeNameTakenException;
import com.licensis.notaire.domain.identification.IdentificationTypeNotFoundException;
import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Maps the catalog's domain exceptions, which carry no HTTP status, to the responses clients already get:
 * an empty 404 and the standard 409 error body. Highest precedence so the generic handler does not take them.
 */
@RestControllerAdvice(assignableTypes = IdentificationTypeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class IdentificationTypeExceptionHandler {

    private final GlobalExceptionHandler global;

    IdentificationTypeExceptionHandler(GlobalExceptionHandler global) {
        this.global = global;
    }

    @ExceptionHandler(IdentificationTypeNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({IdentificationTypeNameTakenException.class, IdentificationTypeInUseException.class})
    ResponseEntity<ErrorResponse> conflict(RuntimeException conflict, HttpServletRequest request) {
        return global.handleNotaireException(new NotaireException(HttpStatus.CONFLICT.value(), conflict.getMessage()),
                request);
    }
}
