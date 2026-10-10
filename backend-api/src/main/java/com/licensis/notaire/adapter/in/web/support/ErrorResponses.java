package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.SQLException;

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

    /**
     * Same text {@code GlobalExceptionHandler} uses for a {@link DataIntegrityViolationException}
     * (issue #579, slice 2).
     */
    public static final String CONSTRAINT_MESSAGE = "The submitted data violates a database constraint";

    /** Safe text for a request that would duplicate an existing record. */
    public static final String DUPLICATE_MESSAGE = "The submitted data duplicates an existing record";

    /**
     * Safe text for an update whose {@code version} is not the stored one (optimistic lock):
     * someone else changed the record since the client read it (issue #655, Owner decision Oct 9).
     */
    public static final String STALE_VERSION_MESSAGE =
            "The record was modified by another user; reload it and try again";

    /** SQLState 23505: unique_violation. */
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    /** SQLState class 23: integrity constraint violation (NOT NULL, FK, unique, check). */
    private static final String SQLSTATE_INTEGRITY_CLASS = "23";

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

    /**
     * Failure of a create (POST). A data-constraint violation is the client's fault and answers
     * {@code 400}, or {@code 409} for a duplicate, as {@code GlobalExceptionHandler} does; anything
     * else keeps {@code 409} (issue #579, slice 2, Owner decisions Run 7 and Oct 9).
     */
    public static ResponseEntity<Object> createFailed(Exception cause) {
        return isConstraintViolation(cause) ? constraintViolation(cause) : conflict(cause);
    }

    /**
     * Failure of an update (PUT). A stale {@code version} (optimistic-lock failure) answers
     * {@code 409} (issue #655, Owner decision Oct 9); a data-constraint violation answers
     * {@code 400}, or {@code 409} for a duplicate; anything else keeps {@code 500} (issue #579).
     */
    public static ResponseEntity<Object> updateFailed(Exception cause) {
        if (isOptimisticLockFailure(cause)) {
            return build(HttpStatus.CONFLICT, STALE_VERSION_MESSAGE, cause);
        }
        return isConstraintViolation(cause) ? constraintViolation(cause) : serverError(cause);
    }

    /** {@code 409 Conflict} for an update whose {@code version} is not the stored one (issue #655). */
    public static ResponseEntity<Object> staleVersion() {
        return build(HttpStatus.CONFLICT, STALE_VERSION_MESSAGE, null);
    }

    /**
     * True when the cause chain holds an optimistic-lock failure: Spring's
     * {@link OptimisticLockingFailureException}, JPA's {@code OptimisticLockException} or
     * Hibernate's {@code StaleStateException}.
     */
    public static boolean isOptimisticLockFailure(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof OptimisticLockingFailureException
                    || t instanceof jakarta.persistence.OptimisticLockException
                    || t instanceof org.hibernate.StaleStateException) {
                return true;
            }
        }
        return false;
    }

    /**
     * Answer for a request whose data violates a database constraint: {@code 409 Conflict} when it
     * would duplicate an existing record (unique constraint), {@code 400 Bad Request} for every
     * other constraint (NOT NULL, foreign key, check). Issue #579, Owner decision Oct 9.
     */
    public static ResponseEntity<Object> constraintViolation(Exception cause) {
        if (isUniqueViolation(cause)) {
            return build(HttpStatus.CONFLICT, DUPLICATE_MESSAGE, cause);
        }
        return build(HttpStatus.BAD_REQUEST, CONSTRAINT_MESSAGE, cause);
    }

    /**
     * True when the cause chain holds a Spring {@link DataIntegrityViolationException}, a
     * Hibernate {@code ConstraintViolationException}, or an {@link SQLException} whose SQLState is
     * in class 23 (integrity constraint violation).
     */
    public static boolean isConstraintViolation(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof DataIntegrityViolationException
                    || t instanceof org.hibernate.exception.ConstraintViolationException) {
                return true;
            }
            if (t instanceof SQLException sql && sql.getSQLState() != null
                    && sql.getSQLState().startsWith(SQLSTATE_INTEGRITY_CLASS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when the cause chain holds a unique-constraint violation: a Spring
     * {@link DuplicateKeyException}, a Hibernate {@code ConstraintViolationException} of kind
     * {@code UNIQUE}, or an {@link SQLException} with SQLState {@code 23505}
     * (issue #579, Owner decision Oct 9: duplicates answer 409).
     */
    public static boolean isUniqueViolation(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof DuplicateKeyException) {
                return true;
            }
            if (t instanceof org.hibernate.exception.ConstraintViolationException hibernate
                    && hibernate.getKind() == org.hibernate.exception.ConstraintViolationException.ConstraintKind.UNIQUE) {
                return true;
            }
            if (t instanceof SQLException sql && SQLSTATE_UNIQUE_VIOLATION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
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
