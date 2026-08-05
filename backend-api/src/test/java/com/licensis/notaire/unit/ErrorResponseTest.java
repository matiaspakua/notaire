package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ErrorResponse should expose its fields for JSON serialization")
class ErrorResponseTest {

    @Test
    @DisplayName("Should serialize all constructor-provided fields, not an empty object")
    void shouldSerializeAllFields() throws Exception {
        ErrorResponse errorResponse = new ErrorResponse(400, "Bad Request", "invalid data", "/api/v1/gestiones");

        String json = new ObjectMapper().writeValueAsString(errorResponse);

        assertThat(json).contains("\"status\":400");
        assertThat(json).contains("\"error\":\"Bad Request\"");
        assertThat(json).contains("\"message\":\"invalid data\"");
        assertThat(json).contains("\"path\":\"/api/v1/gestiones\"");
    }
}
