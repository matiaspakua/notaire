package com.licensis.notaire.application.usecase.mfa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.DisableTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.VerifySecondFactorUseCase;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.MfaEnrollment;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;

@Service
@Transactional
public class TotpDisableService implements DisableTotpUseCase {

    private final MfaEnrollmentStorePort store;
    private final VerifySecondFactorUseCase secondFactor;

    public TotpDisableService(MfaEnrollmentStorePort store, VerifySecondFactorUseCase secondFactor) {
        this.store = store;
        this.secondFactor = secondFactor;
    }

    @Override
    public void disable(int userId, String totpCode, String recoveryCode) {
        store.findByUserId(userId).filter(MfaEnrollment::confirmed).orElseThrow(MfaNotEnrolledException::new);
        if (!secondFactor.verify(userId, totpCode, recoveryCode)) {
            throw new InvalidMfaCodeException();
        }
        store.deleteByUserId(userId);
    }
}
