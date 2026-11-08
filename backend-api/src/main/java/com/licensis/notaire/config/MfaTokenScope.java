package com.licensis.notaire.config;

/** Scopes of the short-lived MFA tokens and the only paths the enrolment token may authenticate (ADR-032). */
public final class MfaTokenScope {

    public static final String CHALLENGE = "mfa_challenge";
    public static final String ENROLLMENT = "mfa_enrollment";
    public static final String ENROLLMENT_AUTHORITY = "ROLE_MFA_ENROLLMENT";
    public static final String ENROLLMENT_PATH_PREFIX = "/api/v1/mfa/enrollment";

    private MfaTokenScope() {
    }

    public static boolean isEnrollmentPath(String requestUri) {
        return requestUri != null && (requestUri.equals(ENROLLMENT_PATH_PREFIX)
                || requestUri.startsWith(ENROLLMENT_PATH_PREFIX + "/"));
    }
}
