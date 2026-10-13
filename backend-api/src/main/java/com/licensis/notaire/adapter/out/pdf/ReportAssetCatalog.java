package com.licensis.notaire.adapter.out.pdf;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * Immutable classpath fonts and the logo contract (issue #1593).
 *
 * <p>No request-selected filesystem path or remote URL is accepted. The Owner has
 * not approved a print-quality mark, so {@link #approvedLogo()} is empty and the
 * renderer draws the text wordmark.
 */
@Component
public class ReportAssetCatalog {

    static final String REGULAR_FONT = "/pdf/fonts/LiberationSans-Regular.ttf";
    static final String BOLD_FONT = "/pdf/fonts/LiberationSans-Bold.ttf";

    public byte[] regularFont() {
        return load(REGULAR_FONT);
    }

    public byte[] boldFont() {
        return load(BOLD_FONT);
    }

    public String wordmark() {
        return ReportTheme.WORDMARK;
    }

    public Optional<byte[]> approvedLogo() {
        return Optional.empty();
    }

    private static byte[] load(String path) {
        try (InputStream in = ReportAssetCatalog.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath PDF asset: " + path);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read classpath PDF asset: " + path, e);
        }
    }
}
