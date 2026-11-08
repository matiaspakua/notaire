package com.licensis.notaire.application.port.in.mfa;

public interface VerifyTotpUseCase {

    boolean verify(int userId, String code);
}
