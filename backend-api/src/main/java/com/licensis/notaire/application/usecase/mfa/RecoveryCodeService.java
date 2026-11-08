package com.licensis.notaire.application.usecase.mfa;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.mfa.UseRecoveryCodeUseCase;
import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.application.port.out.user.PasswordHasherPort;
import com.licensis.notaire.domain.mfa.MfaEnrollment;

@Service
@Transactional
public class RecoveryCodeService implements UseRecoveryCodeUseCase {

    private final MfaEnrollmentStorePort store;
    private final PasswordHasherPort hasher;

    public RecoveryCodeService(MfaEnrollmentStorePort store, PasswordHasherPort hasher) {
        this.store = store;
        this.hasher = hasher;
    }

    @Override
    public boolean use(int userId, String code) {
        if (code == null) {
            return false;
        }
        String normalised = code.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        return store.findByUserId(userId).filter(MfaEnrollment::confirmed).map(enrollment ->
                enrollment.recoveryCodeHashes().stream()
                        .filter(hash -> hasher.passwordMatches(normalised, hash))
                        .findFirst()
                        .map(hash -> {
                            store.save(enrollment.withoutRecoveryCodeHash(hash));
                            return true;
                        }).orElse(false)
        ).orElse(false);
    }
}
