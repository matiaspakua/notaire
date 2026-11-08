package com.licensis.notaire.adapter.out.persistence.mfa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mfa_enrollments")
class MfaEnrollmentJpaEntity {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "secret_ciphertext", nullable = false, columnDefinition = "text")
    private String secretCiphertext;

    @Column(nullable = false)
    private boolean confirmed;

    @Column(name = "last_used_step", nullable = false)
    private long lastUsedStep;

    @Column(name = "recovery_code_hashes", nullable = false, columnDefinition = "text")
    private String recoveryCodeHashes;

    protected MfaEnrollmentJpaEntity() {
    }

    MfaEnrollmentJpaEntity(Integer userId) {
        this.userId = userId;
    }

    Integer getUserId() {
        return userId;
    }

    String getSecretCiphertext() {
        return secretCiphertext;
    }

    void setSecretCiphertext(String secretCiphertext) {
        this.secretCiphertext = secretCiphertext;
    }

    boolean isConfirmed() {
        return confirmed;
    }

    void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    long getLastUsedStep() {
        return lastUsedStep;
    }

    void setLastUsedStep(long lastUsedStep) {
        this.lastUsedStep = lastUsedStep;
    }

    String getRecoveryCodeHashes() {
        return recoveryCodeHashes;
    }

    void setRecoveryCodeHashes(String recoveryCodeHashes) {
        this.recoveryCodeHashes = recoveryCodeHashes;
    }
}
