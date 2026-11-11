package com.licensis.notaire.application.port.in.mfa;

public interface DisableTotpUseCase {

    /**
     * @throws com.licensis.notaire.domain.mfa.MfaNotEnrolledException when no enrolment is confirmed
     * @throws com.licensis.notaire.domain.mfa.InvalidMfaCodeException when neither code is valid
     */
    void disable(int userId, String totpCode, String recoveryCode);
}
