package com.licensis.notaire.adapter.out.persistence.mfa;

import org.springframework.data.jpa.repository.JpaRepository;

interface MfaEnrollmentJpaRepository extends JpaRepository<MfaEnrollmentJpaEntity, Integer> {
}
