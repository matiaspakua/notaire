package com.licensis.notaire.unit.support;

import com.licensis.notaire.adapter.in.web.support.IllegalArgumentResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IllegalArgumentResponses (#1921)")
class IllegalArgumentResponsesTest {

    @Test
    @DisplayName("not-found message maps to 404")
    void shouldMapNotFoundTo404() {
        var response = IllegalArgumentResponses.toResponse(
                new IllegalArgumentException("Payment not found with ID: 1"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(IllegalArgumentResponses.isMissingResource(
                new IllegalArgumentException("Budget not found with ID: 2"))).isTrue();
    }

    @Test
    @DisplayName("validation message maps to 400")
    void shouldMapValidationTo400() {
        var response = IllegalArgumentResponses.toResponse(
                new IllegalArgumentException("The payment amount must be greater than zero"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
