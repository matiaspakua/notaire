package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #579 (slice 1): controller catch blocks must not echo raw exception text
 * (SQL, constraint names, failing-row values, class names) to API clients (CWE-209).
 */
class ErrorResponsesTest {

    static final String SQL_LEAK = "could not execute statement [ERROR: null value in column \"name\" of relation "
            + "\"folio_types\" violates not-null constraint\n  Detail: Failing row contains (7, null, secret-notes)]";

    @Test
    @DisplayName("conflict hides database exception text behind the standard ErrorResponse")
    void conflictHidesDatabaseText() {
        ResponseEntity<Object> response = ErrorResponses.conflict(new DataIntegrityViolationException(SQL_LEAK));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(409);
        assertThat(body.getError()).isEqualTo("Conflict");
        assertThat(body.getMessage()).isEqualTo(ErrorResponses.CONFLICT_MESSAGE);
        assertThat(body.getMessage()).doesNotContain("Failing row", "folio_types", "secret");
    }

    @Test
    @DisplayName("serverError hides exception text and keeps 500")
    void serverErrorHidesText() {
        ResponseEntity<Object> response = ErrorResponses.serverError(
                new IllegalStateException("Row was already updated or deleted by another transaction for entity "
                        + "[com.licensis.notaire.business.Concept with id '1']"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getMessage()).isEqualTo(ErrorResponses.SERVER_ERROR_MESSAGE);
        assertThat(body.getMessage()).doesNotContain("com.licensis");
    }

    @Test
    @DisplayName("application-authored NotaireException messages are kept for the user")
    void applicationMessagesAreKept() {
        ResponseEntity<Object> response = ErrorResponses.conflict(
                new BusinessValidationException("Ya existe un concepto con ese nombre"));

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getMessage()).isEqualTo("Ya existe un concepto con ese nombre");
    }

    @Test
    @DisplayName("a null exception still yields the generic message")
    void nullExceptionIsSafe() {
        ErrorResponse body = (ErrorResponse) ErrorResponses.conflict(null).getBody();
        assertThat(body.getMessage()).isEqualTo(ErrorResponses.CONFLICT_MESSAGE);
    }

    @Test
    @DisplayName("#579 slice 2: a create failing on a data constraint answers 400 with a safe message")
    void createFailedOnConstraintIsBadRequest() {
        ResponseEntity<Object> response = ErrorResponses.createFailed(new DataIntegrityViolationException(SQL_LEAK));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo(ErrorResponses.CONSTRAINT_MESSAGE);
        assertThat(body.getMessage()).doesNotContain("Failing row", "folio_types", "secret");
    }

    @Test
    @DisplayName("#579 slice 2: a constraint violation wrapped in other exceptions is still a 400")
    void wrappedConstraintViolationIsBadRequest() {
        Exception wrapped = new org.springframework.transaction.TransactionSystemException("commit failed",
                new org.hibernate.exception.ConstraintViolationException("x",
                        new java.sql.SQLException("null value", "23502"), "c"));
        assertThat(ErrorResponses.updateFailed(wrapped).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Exception sqlOnly = new RuntimeException(new java.sql.SQLException("unique", "23505"));
        assertThat(ErrorResponses.createFailed(sqlOnly).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("#579 slice 2: other failures keep their status (409 on create, 500 on update)")
    void otherFailuresKeepTheirStatus() {
        assertThat(ErrorResponses.createFailed(new RuntimeException("x")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorResponses.updateFailed(new RuntimeException("x")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ErrorResponses.updateFailed(new java.sql.SQLException("deadlock", "40001")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
