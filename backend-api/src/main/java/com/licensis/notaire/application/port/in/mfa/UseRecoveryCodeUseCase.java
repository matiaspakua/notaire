package com.licensis.notaire.application.port.in.mfa;

public interface UseRecoveryCodeUseCase {

    boolean use(int userId, String code);
}
