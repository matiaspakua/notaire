package com.licensis.notaire.adapter.out.persistence.mfa;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.application.port.out.mfa.SecretCipherPort;
import com.licensis.notaire.domain.mfa.MfaEnrollment;

/** Stores the TOTP secret as ciphertext only (ADR-032); BCrypt hashes contain no newline. */
@Component
public class MfaEnrollmentPersistenceAdapter implements MfaEnrollmentStorePort {

    private static final String HASH_SEPARATOR = "\n";

    private final MfaEnrollmentJpaRepository repository;
    private final SecretCipherPort cipher;

    public MfaEnrollmentPersistenceAdapter(MfaEnrollmentJpaRepository repository, SecretCipherPort cipher) {
        this.repository = repository;
        this.cipher = cipher;
    }

    @Override
    public Optional<MfaEnrollment> findByUserId(int userId) {
        return repository.findById(userId).map(this::toDomain);
    }

    @Override
    public MfaEnrollment save(MfaEnrollment enrollment) {
        MfaEnrollmentJpaEntity entity = repository.findById(enrollment.userId())
                .orElseGet(() -> new MfaEnrollmentJpaEntity(enrollment.userId()));
        entity.setSecretCiphertext(cipher.encrypt(enrollment.secretBase32()));
        entity.setConfirmed(enrollment.confirmed());
        entity.setLastUsedStep(enrollment.lastUsedStep());
        entity.setRecoveryCodeHashes(String.join(HASH_SEPARATOR, enrollment.recoveryCodeHashes()));
        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteByUserId(int userId) {
        repository.deleteById(userId);
    }

    private MfaEnrollment toDomain(MfaEnrollmentJpaEntity entity) {
        String hashes = entity.getRecoveryCodeHashes();
        List<String> recoveryHashes = hashes.isEmpty() ? List.of() : Arrays.asList(hashes.split(HASH_SEPARATOR));
        return new MfaEnrollment(entity.getUserId(), cipher.decrypt(entity.getSecretCiphertext()),
                entity.isConfirmed(), entity.getLastUsedStep(), recoveryHashes);
    }
}
