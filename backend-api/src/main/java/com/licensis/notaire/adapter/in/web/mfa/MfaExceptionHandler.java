package com.licensis.notaire.adapter.in.web.mfa;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.MfaAlreadyEnrolledException;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;
import com.licensis.notaire.domain.user.LoginLockedException;

/** Maps MFA domain exceptions to HTTP; messages never echo submitted codes. */
@RestControllerAdvice(assignableTypes = MfaController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MfaExceptionHandler {

    @ExceptionHandler(InvalidMfaCodeException.class)
    ResponseEntity<ErrorBody> invalidCode(InvalidMfaCodeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorBody(e.getMessage()));
    }

    @ExceptionHandler(MfaNotEnrolledException.class)
    ResponseEntity<ErrorBody> notEnrolled(MfaNotEnrolledException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorBody(e.getMessage()));
    }

    @ExceptionHandler(MfaAlreadyEnrolledException.class)
    ResponseEntity<ErrorBody> alreadyEnrolled(MfaAlreadyEnrolledException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorBody(e.getMessage()));
    }

    @ExceptionHandler(LoginLockedException.class)
    ResponseEntity<ErrorBody> locked(LoginLockedException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorBody(e.getMessage()));
    }

    record ErrorBody(String message) { }
}
