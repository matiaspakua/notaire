package com.licensis.notaire.adapter.in.web.registrationdraft;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.registrationdraft.RegistrationDraftBadRequestException;
import com.licensis.notaire.domain.registrationdraft.RegistrationDraftNotFoundException;

/**
 * Maps registration-draft domain exceptions to the HTTP shapes clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = RegistrationDraftController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RegistrationDraftExceptionHandler {

    @ExceptionHandler(RegistrationDraftNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RegistrationDraftBadRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(RegistrationDraftBadRequestException bad) {
        return ResponseEntity.badRequest().body(Map.of("message", bad.getMessage()));
    }
}
