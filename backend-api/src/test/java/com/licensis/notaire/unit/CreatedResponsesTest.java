package com.licensis.notaire.unit;

import com.licensis.notaire.adapter.in.web.support.CreatedResponses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreatedResponses helper")
class CreatedResponsesTest {

    @Test
    @DisplayName("should return 201 with Location for collection path and id")
    void shouldReturnCreatedWithLocation() {
        ResponseEntity<String> response = CreatedResponses.of("body", "/api/v1/pagos", 42);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("body");
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().getPath()).isEqualTo("/api/v1/pagos/42");
    }
}
