package com.licensis.notaire.domain.mfa;

public class InvalidMfaCodeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidMfaCodeException() {
        super("The submitted verification code is not valid");
    }
}
