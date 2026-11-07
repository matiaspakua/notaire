package com.licensis.notaire.adapter.in.web.procedure;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.procedure.ProcedureNotFoundException;

/**
 * Maps procedure domain exceptions to the HTTP shapes clients already receive from
 * {@link ProcedureController} write methods (#577). ServerError still throws to GEH.
 */
@RestControllerAdvice(assignableTypes = ProcedureController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProcedureExceptionHandler {

    @ExceptionHandler(ProcedureNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
