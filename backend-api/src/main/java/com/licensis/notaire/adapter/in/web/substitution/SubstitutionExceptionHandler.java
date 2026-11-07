package com.licensis.notaire.adapter.in.web.substitution;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.substitution.SubstitutionBadRequestException;
import com.licensis.notaire.domain.substitution.SubstitutionNotFoundException;

/**
 * Maps substitution domain exceptions to empty 400/404 bodies (#577).
 */
@RestControllerAdvice(assignableTypes = SubstitutionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SubstitutionExceptionHandler {

    @ExceptionHandler(SubstitutionBadRequestException.class)
    ResponseEntity<Void> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(SubstitutionNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
