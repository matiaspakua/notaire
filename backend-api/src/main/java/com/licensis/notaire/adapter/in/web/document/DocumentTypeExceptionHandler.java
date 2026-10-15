package com.licensis.notaire.adapter.in.web.document;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.document.DocumentTypeInUseException;
import com.licensis.notaire.domain.document.DocumentTypeInvalidException;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;
import com.licensis.notaire.domain.document.DocumentTypeReferencedException;
import com.licensis.notaire.domain.document.DocumentTypeStaleVersionException;

/**
 * Maps the document type domain exceptions, which carry no HTTP status, to the responses clients already get: an
 * empty 404, the {@code {"error": …}} 409 of a locked type, the standard stale-version 409 and the standard
 * constraint 400. Highest precedence so the generic handler does not take them.
 */
@RestControllerAdvice(assignableTypes = DocumentTypeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class DocumentTypeExceptionHandler {

    @ExceptionHandler(DocumentTypeNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(DocumentTypeInUseException.class)
    ResponseEntity<Map<String, String>> inUse(DocumentTypeInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(DocumentTypeReferencedException.class)
    ResponseEntity<Map<String, String>> referenced(DocumentTypeReferencedException referenced) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", referenced.getMessage()));
    }

    @ExceptionHandler(DocumentTypeStaleVersionException.class)
    ResponseEntity<Object> staleVersion() {
        return ErrorResponses.staleVersion();
    }

    @ExceptionHandler(DocumentTypeInvalidException.class)
    ResponseEntity<Object> invalid(DocumentTypeInvalidException invalid) {
        return ErrorResponses.constraintViolation(invalid);
    }
}
