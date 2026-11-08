package com.licensis.notaire.application.port.in.mfa;

public interface VerifySecondFactorUseCase {

    /**
     * Checks a recovery code when one is given, otherwise a TOTP code. A recovery code is consumed on success.
     */
    boolean verify(int userId, String totpCode, String recoveryCode);
}
