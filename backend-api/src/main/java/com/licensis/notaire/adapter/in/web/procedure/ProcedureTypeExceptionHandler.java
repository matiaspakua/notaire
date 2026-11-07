package com.licensis.notaire.adapter.in.web.procedure;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.procedure.ProcedureTypeInUseException;
import com.licensis.notaire.domain.procedure.ProcedureTypeNotFoundException;
import com.licensis.notaire.domain.procedure.ProcedureTypeOperationFailedException;

/**
 * Maps procedure-type domain exceptions to the HTTP shapes clients already receive from
 * {@link ProcedureTypeController} write methods (#577).
 */
@RestControllerAdvice(assignableTypes = ProcedureTypeController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProcedureTypeExceptionHandler {

    @ExceptionHandler(ProcedureTypeNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ProcedureTypeInUseException.class)
    ResponseEntity<Map<String, String>> inUse(ProcedureTypeInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", locked.getMessage()));
    }

    @ExceptionHandler(ProcedureTypeOperationFailedException.class)
    ResponseEntity<Object> failed(ProcedureTypeOperationFailedException failed) {
        return switch (failed.kind()) {
            case CREATE -> ErrorResponses.createFailed(failed.causeAsException());
            case UPDATE -> ErrorResponses.updateFailed(failed.causeAsException());
        };
    }
}
