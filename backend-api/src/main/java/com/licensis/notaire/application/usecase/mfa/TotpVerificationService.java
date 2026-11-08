package com.licensis.notaire.application.usecase.mfa;

import java.util.OptionalLong;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.VerifyTotpUseCase;
import com.licensis.notaire.application.port.out.mfa.ClockPort;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;

@Service
@Transactional
public class TotpVerificationService implements VerifyTotpUseCase {

    private final MfaEnrollmentStorePort store;
    private final ClockPort clock;

    public TotpVerificationService(MfaEnrollmentStorePort store, ClockPort clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public boolean verify(int userId, String code) {
        return store.findByUserId(userId).filter(e -> e.confirmed()).map(enrollment -> {
            OptionalLong step = TotpAlgorithm.matchingStep(Base32Codec.decode(enrollment.secretBase32()), code,
                    TotpAlgorithm.stepAt(clock.now()));
            if (step.isEmpty() || step.getAsLong() <= enrollment.lastUsedStep()) {
                return false;
            }
            store.save(enrollment.withLastUsedStep(step.getAsLong()));
            return true;
        }).orElse(false);
    }
}
