package com.licensis.notaire.repository;

import com.licensis.notaire.business.IdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentificationTypeRepository extends JpaRepository<IdentificationType, Integer> {

    Optional<IdentificationType> findByName(String name);

    boolean existsByName(String name);
}
