package com.licensis.notaire.adapter.in.web.procedure;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.procedure.ProcedureFolderBadRequestException;
import com.licensis.notaire.domain.procedure.ProcedureFolderNotFoundException;

/**
 * Maps procedure-folder domain exceptions to the HTTP shapes clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = ProcedureFolderController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProcedureFolderExceptionHandler {

    @ExceptionHandler(ProcedureFolderNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ProcedureFolderBadRequestException.class)
    ResponseEntity<Map<String, String>> badRequest(ProcedureFolderBadRequestException bad) {
        return ResponseEntity.badRequest().body(Map.of("message", bad.getMessage()));
    }
}
