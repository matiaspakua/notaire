package com.licensis.notaire.adapter.in.web.folio;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.folio.FolioTypeInUseException;
import com.licensis.notaire.domain.folio.FolioTypeInvalidException;
import com.licensis.notaire.domain.folio.FolioTypeNotFoundException;
import com.licensis.notaire.domain.folio.FolioTypeReferencedException;
import com.licensis.notaire.domain.folio.FolioTypeStaleVersionException;

/**
 * Maps the folio type domain exceptions, which carry no HTTP status, to the responses clients already get: an
 * empty 404, the {@code {"error": …}} 409 of a locked type, the plain-text 409 of a referenced type, the standard
 * stale-version 409 and the standard constraint 400. Highest precedence so the generic handler does not take them.
 */
@RestControllerAdvice(assignableTypes = FolioTypeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class FolioTypeExceptionHandler {

    @ExceptionHandler(FolioTypeNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(FolioTypeInUseException.class)
    ResponseEntity<Map<String, String>> inUse(FolioTypeInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(FolioTypeReferencedException.class)
    ResponseEntity<String> referenced(FolioTypeReferencedException referenced) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(referenced.getMessage());
    }

    @ExceptionHandler(FolioTypeStaleVersionException.class)
    ResponseEntity<Object> staleVersion() {
        return ErrorResponses.staleVersion();
    }

    @ExceptionHandler(FolioTypeInvalidException.class)
    ResponseEntity<Object> invalid(FolioTypeInvalidException invalid) {
        return ErrorResponses.constraintViolation(invalid);
    }
}
