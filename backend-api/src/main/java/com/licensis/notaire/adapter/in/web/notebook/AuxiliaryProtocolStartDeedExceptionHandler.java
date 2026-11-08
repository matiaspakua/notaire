package com.licensis.notaire.adapter.in.web.notebook;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.notebook.AuxiliaryProtocolStartBadRequestException;
import com.licensis.notaire.domain.notebook.AuxiliaryProtocolStartNotFoundException;

@RestControllerAdvice(assignableTypes = AuxiliaryProtocolController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuxiliaryProtocolStartDeedExceptionHandler {

    @ExceptionHandler(AuxiliaryProtocolStartBadRequestException.class)
    ResponseEntity<StartDeedErrorResponse> badRequest(AuxiliaryProtocolStartBadRequestException failure) {
        return ResponseEntity.badRequest().body(new StartDeedErrorResponse(failure.getMessage()));
    }

    @ExceptionHandler(AuxiliaryProtocolStartNotFoundException.class)
    ResponseEntity<StartDeedErrorResponse> notFound(AuxiliaryProtocolStartNotFoundException failure) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StartDeedErrorResponse(failure.getMessage()));
    }
}
