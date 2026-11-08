package com.licensis.notaire.domain.mfa;

public class MfaAlreadyEnrolledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MfaAlreadyEnrolledException() {
        super("Multi-factor authentication is already enabled");
    }
}
