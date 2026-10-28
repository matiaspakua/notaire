package com.licensis.notaire.adapter.in.web.support;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Maps {@link IllegalArgumentException} from use cases to HTTP status (#1921).
 *
 * <p>Missing-resource messages (containing {@code not found}, case-insensitive) answer
 * {@code 404}; every other invalid-argument case answers {@code 400}.
 */
public final class IllegalArgumentResponses {

    private IllegalArgumentResponses() {
    }

    public static boolean isMissingResource(IllegalArgumentException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains("not found");
    }

    public static ResponseEntity<Void> toResponse(IllegalArgumentException exception) {
        return status(exception).build();
    }

    /** Empty body with 404 or 400 for endpoints that normally return a typed body. */
    public static <T> ResponseEntity<T> toEmptyBody(IllegalArgumentException exception) {
        return status(exception).build();
    }

    private static ResponseEntity.BodyBuilder status(IllegalArgumentException exception) {
        HttpStatus httpStatus = isMissingResource(exception) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus);
    }
}
