package com.licensis.notaire.adapter.in.web.testimony;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.testimony.TestimonyConflictException;
import com.licensis.notaire.domain.testimony.TestimonyNotFoundException;
import com.licensis.notaire.domain.testimony.TestimonyOperationFailedException;
import com.licensis.notaire.domain.testimony.TestimonyStaleVersionException;
import com.licensis.notaire.domain.testimony.TestimonyUnexpectedResultException;

/**
 * Maps testimony-movement write failures to the HTTP shapes clients already receive (#577).
 * Reuses the testimony domain exception types — same status and body contracts.
 */
@RestControllerAdvice(assignableTypes = TestimonyMovementController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestimonyMovementExceptionHandler {

    @ExceptionHandler(TestimonyNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(TestimonyConflictException.class)
    ResponseEntity<String> conflict(TestimonyConflictException conflict) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(conflict.getMessage());
    }

    @ExceptionHandler(TestimonyStaleVersionException.class)
    ResponseEntity<Object> staleVersion() {
        return ErrorResponses.staleVersion();
    }

    @ExceptionHandler(TestimonyOperationFailedException.class)
    ResponseEntity<Object> failed(TestimonyOperationFailedException failed) {
        return switch (failed.operation()) {
            case CREATE -> ErrorResponses.createFailed(failed.causeAsException());
            case UPDATE -> ErrorResponses.updateFailed(failed.causeAsException());
        };
    }

    @ExceptionHandler(TestimonyUnexpectedResultException.class)
    ResponseEntity<Void> unexpected() {
        return ResponseEntity.internalServerError().build();
    }
}
