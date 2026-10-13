package com.licensis.notaire.adapter.out.pdf;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;

/**
 * Neutralizes control / bidi characters and drops glyphs the embedded font
 * cannot encode. User text is written as PDF text operands only.
 */
public final class ReportTextSanitizer {

    static final int MAX_CHARS = 8_000;

    private ReportTextSanitizer() {
    }

    public static String neutralize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(Math.min(text.length(), MAX_CHARS));
        text.codePoints().forEach(codePoint -> {
            if (out.length() >= MAX_CHARS) {
                return;
            }
            if (isUnsafe(codePoint)) {
                out.append(' ');
                return;
            }
            out.appendCodePoint(codePoint);
        });
        return out.toString().replaceAll(" {2,}", " ").trim();
    }

    public static String sanitize(PDFont font, String text) {
        String cleaned = neutralize(text);
        if (cleaned.isEmpty() || font == null) {
            return cleaned;
        }
        StringBuilder out = new StringBuilder(cleaned.length());
        cleaned.codePoints().forEach(codePoint -> {
            String character = new String(Character.toChars(codePoint));
            try {
                font.encode(character);
                out.append(character);
            } catch (IOException | IllegalArgumentException | UnsupportedOperationException e) {
                out.append('?');
            }
        });
        return out.toString();
    }

    private static boolean isUnsafe(int codePoint) {
        if (Character.isISOControl(codePoint) || Character.isWhitespace(codePoint)) {
            return true;
        }
        return codePoint >= 0x202A && codePoint <= 0x202E
                || codePoint >= 0x2066 && codePoint <= 0x2069
                || codePoint == 0x200E || codePoint == 0x200F
                || codePoint == 0x061C;
    }
}
