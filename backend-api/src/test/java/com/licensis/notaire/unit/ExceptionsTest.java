package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ErrorResponse;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception classes - unit tests")
class ExceptionsTest {

    @Nested
    @DisplayName("NotaireException")
    class NotaireExceptionTests {

        @Test
        @DisplayName("Should default status code to 500 when only message provided")
        void shouldDefaultStatusCodeTo500WhenOnlyMessage() {
            NotaireException ex = new NotaireException("boom");
            assertThat(ex.getMessage()).isEqualTo("boom");
            assertThat(ex.getStatusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should default status code to 500 when message and cause provided")
        void shouldDefaultStatusCodeTo500WhenMessageAndCause() {
            Throwable cause = new IllegalStateException("root cause");
            NotaireException ex = new NotaireException("boom", cause);
            assertThat(ex.getMessage()).isEqualTo("boom");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getStatusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should accept custom status code")
        void shouldAcceptCustomStatusCode() {
            NotaireException ex = new NotaireException(418, "teapot");
            assertThat(ex.getStatusCode()).isEqualTo(418);
            assertThat(ex.getMessage()).isEqualTo("teapot");
        }

        @Test
        @DisplayName("Should accept custom status code with cause")
        void shouldAcceptCustomStatusCodeWithCause() {
            Throwable cause = new RuntimeException("cause");
            NotaireException ex = new NotaireException(418, "teapot", cause);
            assertThat(ex.getStatusCode()).isEqualTo(418);
            assertThat(ex.getMessage()).isEqualTo("teapot");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Should produce 404 from message-only constructor")
        void shouldProduce404FromMessageOnly() {
            ResourceNotFoundException ex = new ResourceNotFoundException("missing");
            assertThat(ex.getStatusCode()).isEqualTo(404);
            assertThat(ex.getMessage()).isEqualTo("missing");
        }

        @Test
        @DisplayName("Should produce 404 from message and cause constructor")
        void shouldProduce404FromMessageAndCause() {
            Throwable cause = new RuntimeException("root");
            ResourceNotFoundException ex = new ResourceNotFoundException("missing", cause);
            assertThat(ex.getStatusCode()).isEqualTo(404);
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("BusinessValidationException")
    class BusinessValidationExceptionTests {

        @Test
        @DisplayName("Should produce 400 from message-only constructor")
        void shouldProduce400FromMessageOnly() {
            BusinessValidationException ex = new BusinessValidationException("invalid");
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getMessage()).isEqualTo("invalid");
        }

        @Test
        @DisplayName("Should produce 400 from message and cause constructor")
        void shouldProduce400FromMessageAndCause() {
            Throwable cause = new IllegalArgumentException("bad");
            BusinessValidationException ex = new BusinessValidationException("invalid", cause);
            assertThat(ex.getStatusCode()).isEqualTo(400);
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ErrorResponse")
    class ErrorResponseTests {

        @Test
        @DisplayName("Default constructor should run without throwing")
        void defaultConstructorShouldRun() {
            ErrorResponse er = new ErrorResponse();
            assertThat(er).isNotNull();
        }

        @Test
        @DisplayName("Three-arg constructor should be usable")
        void threeArgConstructorShouldBeUsable() {
            ErrorResponse er = new ErrorResponse(400, "Bad Request", "msg");
            assertThat(er).isNotNull();
        }

        @Test
        @DisplayName("Four-arg constructor should be usable")
        void fourArgConstructorShouldBeUsable() {
            ErrorResponse er = new ErrorResponse(500, "Internal", "err", "/api/x");
            assertThat(er).isNotNull();
        }
    }
}
