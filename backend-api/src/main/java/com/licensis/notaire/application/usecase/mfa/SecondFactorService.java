package com.licensis.notaire.application.usecase.mfa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.UseRecoveryCodeUseCase;
import com.licensis.notaire.application.port.in.mfa.VerifySecondFactorUseCase;
import com.licensis.notaire.application.port.in.mfa.VerifyTotpUseCase;

@Service
@Transactional
public class SecondFactorService implements VerifySecondFactorUseCase {

    private final VerifyTotpUseCase totp;
    private final UseRecoveryCodeUseCase recovery;

    public SecondFactorService(VerifyTotpUseCase totp, UseRecoveryCodeUseCase recovery) {
        this.totp = totp;
        this.recovery = recovery;
    }

    @Override
    public boolean verify(int userId, String totpCode, String recoveryCode) {
        if (recoveryCode != null && !recoveryCode.isBlank()) {
            return recovery.use(userId, recoveryCode);
        }
        return totpCode != null && !totpCode.isBlank() && totp.verify(userId, totpCode);
    }
}
