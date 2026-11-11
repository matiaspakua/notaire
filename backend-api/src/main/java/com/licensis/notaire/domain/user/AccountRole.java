package com.licensis.notaire.domain.user;

import java.util.Locale;

/** Role derived from the stored user type; the single mapping used by authorization and the MFA policy. */
public enum AccountRole {
    ADMIN,
    NOTARY,
    USER;

    public static AccountRole ofUserType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ADMIN", "ADMINISTRADOR" -> ADMIN;
            case "ESCRIBANO" -> NOTARY;
            default -> USER;
        };
    }
}
