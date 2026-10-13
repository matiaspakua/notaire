package com.licensis.notaire.adapter.in.web.report;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

/**
 * Centralized PDF HTTP headers (issue #1593).
 */
public final class PdfResponseFactory {

    public static final String CACHE_CONTROL = "private, no-store";
    public static final String NOSNIFF = "nosniff";

    private PdfResponseFactory() {
    }

    public static ResponseEntity<byte[]> ok(byte[] pdf, String filename) {
        byte[] body = pdf == null ? new byte[0] : pdf;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeFilename(filename) + "\"");
        headers.set(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL);
        headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length));
        headers.set("X-Content-Type-Options", NOSNIFF);
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }

    static String sanitizeFilename(String filename) {
        String base = filename == null ? "report.pdf" : filename.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        String cleaned = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (cleaned.isBlank()) {
            cleaned = "report.pdf";
        }
        if (!cleaned.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            cleaned = cleaned + ".pdf";
        }
        return cleaned;
    }
}
