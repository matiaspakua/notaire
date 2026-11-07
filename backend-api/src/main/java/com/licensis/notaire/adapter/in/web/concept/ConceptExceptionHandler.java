package com.licensis.notaire.adapter.in.web.concept;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.concept.ConceptInUseException;
import com.licensis.notaire.domain.concept.ConceptNotFoundException;
import com.licensis.notaire.domain.concept.ConceptOperationFailedException;

/**
 * Maps concept domain exceptions to the HTTP shapes clients already receive from
 * {@link ConceptController} write methods (empty 404, {@code {"error"}} 409, ErrorResponses bodies).
 */
@RestControllerAdvice(assignableTypes = ConceptController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConceptExceptionHandler {

    @ExceptionHandler(ConceptNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ConceptInUseException.class)
    ResponseEntity<Map<String, String>> inUse(ConceptInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(ConceptOperationFailedException.class)
    ResponseEntity<Object> failed(ConceptOperationFailedException failed) {
        return switch (failed.operation()) {
            case CREATE -> ErrorResponses.createFailed(failed.causeAsException());
            case UPDATE -> ErrorResponses.updateFailed(failed.causeAsException());
            case DELETE -> ErrorResponses.conflict(failed.causeAsException());
        };
    }
}
