package com.licensis.notaire.domain.mfa;

import java.util.Locale;

/** OFF skips every challenge; OPTIONAL honours voluntary enrolments; ENFORCE also requires them for sensitive roles. */
public enum MfaPolicyMode {
    OFF,
    OPTIONAL,
    ENFORCE;

    public static MfaPolicyMode parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return OPTIONAL;
        }
        return valueOf(raw.trim().toUpperCase(Locale.ROOT));
    }
}
