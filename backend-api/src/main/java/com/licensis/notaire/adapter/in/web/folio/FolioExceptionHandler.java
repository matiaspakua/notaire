package com.licensis.notaire.adapter.in.web.folio;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.folio.FolioInUseException;
import com.licensis.notaire.domain.folio.FolioInvalidException;
import com.licensis.notaire.domain.folio.FolioNotFoundException;

/**
 * Maps Folio domain exceptions to the HTTP shapes FolioController already returned (#577).
 */
@RestControllerAdvice(assignableTypes = FolioController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class FolioExceptionHandler {

    @ExceptionHandler(FolioNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(FolioInUseException.class)
    ResponseEntity<Map<String, String>> inUse(FolioInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(FolioInvalidException.class)
    ResponseEntity<Void> invalid() {
        return ResponseEntity.badRequest().build();
    }
}
