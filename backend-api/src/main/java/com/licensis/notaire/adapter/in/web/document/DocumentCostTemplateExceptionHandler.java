package com.licensis.notaire.adapter.in.web.document;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.document.DocumentCostTemplateInvalidException;
import com.licensis.notaire.domain.document.DocumentCostTemplateNotFoundException;

/**
 * Maps document-cost-template create failures to {@code {"message":…}} bodies (#577).
 */
@RestControllerAdvice(assignableTypes = DocumentCostTemplateController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DocumentCostTemplateExceptionHandler {

    @ExceptionHandler(DocumentCostTemplateInvalidException.class)
    ResponseEntity<Map<String, String>> invalid(DocumentCostTemplateInvalidException invalid) {
        return ResponseEntity.badRequest().body(Map.of("message", invalid.getMessage()));
    }

    @ExceptionHandler(DocumentCostTemplateNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(DocumentCostTemplateNotFoundException missing) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", missing.getMessage()));
    }
}
