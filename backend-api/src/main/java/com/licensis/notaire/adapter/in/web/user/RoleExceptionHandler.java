package com.licensis.notaire.adapter.in.web.user;

import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.RoleNotFoundException;
import com.licensis.notaire.domain.user.UserNotFoundException;

/**
 * Maps role/user domain exceptions for {@link RoleController} to existing HTTP shapes (#577).
 */
@RestControllerAdvice(assignableTypes = RoleController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RoleExceptionHandler {

    @ExceptionHandler(RoleNotFoundException.class)
    ResponseEntity<Void> roleNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<Void> userNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RoleNameConflictException.class)
    ResponseEntity<Map<String, String>> nameConflict(RoleNameConflictException conflict) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", conflict.getMessage()));
    }
}
