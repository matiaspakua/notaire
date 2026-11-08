package com.licensis.notaire.application.port.in.mfa;

/** Secret to show once and the otpauth URI for the QR code. */
public record TotpEnrollmentStart(String secret, String otpauthUri) {
}
