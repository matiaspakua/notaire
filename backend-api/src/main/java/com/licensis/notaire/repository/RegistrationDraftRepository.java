package com.licensis.notaire.repository;

import com.licensis.notaire.business.RegistrationDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationDraftRepository extends JpaRepository<RegistrationDraft, Integer> {

    Optional<RegistrationDraft> findByFkIdDeedIdDeed(Integer idDeed);

    Optional<RegistrationDraft> findTopByOrderByNumberDesc();
}
