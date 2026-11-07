package com.licensis.notaire.adapter.in.web.user;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.user.LoginLockedException;
import com.licensis.notaire.domain.user.LoginRejectedException;

/**
 * Maps login domain exceptions to the HTTP shapes clients already receive (#577).
 */
@RestControllerAdvice(assignableTypes = UserController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserLoginExceptionHandler {

    @ExceptionHandler(LoginRejectedException.class)
    ResponseEntity<InvalidLoginResponse> rejected() {
        return ResponseEntity.ok(new InvalidLoginResponse(false));
    }

    @ExceptionHandler(LoginLockedException.class)
    ResponseEntity<LockedLoginResponse> locked(LoginLockedException locked) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new LockedLoginResponse(false, locked.getMessage()));
    }

    record InvalidLoginResponse(boolean valid) { }

    record LockedLoginResponse(boolean valid, String message) { }
}
