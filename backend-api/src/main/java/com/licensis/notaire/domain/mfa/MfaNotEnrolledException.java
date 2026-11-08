package com.licensis.notaire.domain.mfa;

public class MfaNotEnrolledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MfaNotEnrolledException() {
        super("Multi-factor enrolment has not been started");
    }
}
