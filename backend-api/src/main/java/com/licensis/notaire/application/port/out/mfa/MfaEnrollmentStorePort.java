package com.licensis.notaire.application.port.out.mfa;

import java.util.Optional;

import com.licensis.notaire.domain.mfa.MfaEnrollment;

public interface MfaEnrollmentStorePort {

    Optional<MfaEnrollment> findByUserId(int userId);

    MfaEnrollment save(MfaEnrollment enrollment);

    void deleteByUserId(int userId);
}
