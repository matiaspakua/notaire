package com.licensis.notaire.application.port.in.mfa;

import java.util.List;

public interface EnrollTotpUseCase {

    /** @throws com.licensis.notaire.domain.mfa.MfaAlreadyEnrolledException when already confirmed */
    TotpEnrollmentStart begin(int userId, String accountName);

    /**
     * @return the recovery codes, shown once
     * @throws com.licensis.notaire.domain.mfa.MfaNotEnrolledException when not begun
     * @throws com.licensis.notaire.domain.mfa.InvalidMfaCodeException when the code is wrong
     */
    List<String> confirm(int userId, String code);
}
