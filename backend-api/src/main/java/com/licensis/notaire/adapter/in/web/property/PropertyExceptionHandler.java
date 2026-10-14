package com.licensis.notaire.adapter.in.web.property;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.property.PropertyNotFoundException;

/** Maps the domain not-found exception, which carries no HTTP status, to the empty 404 clients already get. */
@RestControllerAdvice(assignableTypes = PropertyController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class PropertyExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
