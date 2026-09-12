package com.licensis.notaire.repository;

import com.licensis.notaire.business.ManagementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagementStatusRepository extends JpaRepository<ManagementStatus, Integer> {

    Optional<ManagementStatus> findByName(String name);

    boolean existsByName(String name);

    List<ManagementStatus> findByNameContaining(String name);
}
