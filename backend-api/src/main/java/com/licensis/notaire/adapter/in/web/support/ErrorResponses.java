package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Safe error bodies for controllers that still catch exceptions themselves.
 *
 * <p>Issue #579 (slice 1): a caught exception's text can carry SQL, table and column names,
 * entity class names and "Failing row contains (...)" values, so it is logged on the server
 * and never sent to the client (CWE-209). Only messages written by the application
 * ({@link NotaireException} and subclasses) reach the user. The body is the same
 * {@link ErrorResponse} shape that {@code GlobalExceptionHandler} returns.
 */
public final class ErrorResponses {

    /** Generic text for a request that failed on a data constraint or conflicting state. */
    public static final String CONFLICT_MESSAGE =
            "The request conflicts with existing data or violates a data constraint";

    /** Same text {@code GlobalExceptionHandler} uses for unexpected failures. */
    public static final String SERVER_ERROR_MESSAGE = "An unexpected error occurred";

    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponses.class);

    private ErrorResponses() {
    }

    /** {@code 409 Conflict} with a safe message. */
    public static ResponseEntity<Object> conflict(Exception cause) {
        return build(HttpStatus.CONFLICT, CONFLICT_MESSAGE, cause);
    }

    /** {@code 500 Internal Server Error} with a safe message. */
    public static ResponseEntity<Object> serverError(Exception cause) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, SERVER_ERROR_MESSAGE, cause);
    }

    private static ResponseEntity<Object> build(HttpStatus status, String fallback, Exception cause) {
        String path = currentPath();
        String message = cause instanceof NotaireException && cause.getMessage() != null
                ? cause.getMessage()
                : fallback;
        if (status.is5xxServerError()) {
            LOG.error("Request failed with {} on {}", status.value(), path, cause);
        } else {
            LOG.warn("Request failed with {} on {}: {}", status.value(), path,
                    cause == null ? "-" : cause.getClass().getSimpleName(), cause);
        }
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(body);
    }

    private static String currentPath() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        }
        return null;
    }
}
