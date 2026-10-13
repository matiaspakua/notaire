package com.licensis.notaire.adapter.in.web.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PdfResponseFactory (issue #1593)")
class PdfResponseFactoryTest {

    @Test
    @DisplayName("shouldSendSafePdfHeaders")
    void shouldSendSafePdfHeaders() {
        byte[] pdf = {0x25, 0x50, 0x44, 0x46};
        ResponseEntity<byte[]> response = PdfResponseFactory.ok(pdf, "../../secret/name with spaces.pdf");

        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)).isEqualTo("application/pdf");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("inline; filename=\"name_with_spaces.pdf\"");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL)).isEqualTo("private, no-store");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_LENGTH)).isEqualTo("4");
        assertThat(response.getBody()).isEqualTo(pdf);
    }
}
