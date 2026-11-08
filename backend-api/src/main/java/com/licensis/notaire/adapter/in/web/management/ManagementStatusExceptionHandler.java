package com.licensis.notaire.adapter.in.web.management;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.management.ManagementStatusInUseException;
import com.licensis.notaire.domain.management.ManagementStatusInvalidException;
import com.licensis.notaire.domain.management.ManagementStatusNotFoundException;
import com.licensis.notaire.domain.management.ManagementStatusReferencedException;
import com.licensis.notaire.domain.management.ManagementStatusStaleVersionException;

/**
 * Maps the management status domain exceptions, which carry no HTTP status, to the responses clients already get: an
 * empty 404, the {@code {"error": …}} 409 of a locked status, the standard stale-version 409 and the standard
 * constraint 400. Highest precedence so the generic handler does not take them.
 */
@RestControllerAdvice(assignableTypes = ManagementStatusController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class ManagementStatusExceptionHandler {

    @ExceptionHandler(ManagementStatusNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ManagementStatusInUseException.class)
    ResponseEntity<Map<String, String>> inUse(ManagementStatusInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(ManagementStatusReferencedException.class)
    ResponseEntity<Map<String, String>> referenced(ManagementStatusReferencedException referenced) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", referenced.getMessage()));
    }

    @ExceptionHandler(ManagementStatusStaleVersionException.class)
    ResponseEntity<Object> staleVersion() {
        return ErrorResponses.staleVersion();
    }

    @ExceptionHandler(ManagementStatusInvalidException.class)
    ResponseEntity<Object> invalid(ManagementStatusInvalidException invalid) {
        return ErrorResponses.constraintViolation(invalid);
    }
}
