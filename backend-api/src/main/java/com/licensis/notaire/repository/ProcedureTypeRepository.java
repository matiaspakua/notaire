package com.licensis.notaire.repository;

import com.licensis.notaire.business.ProcedureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureTypeRepository extends JpaRepository<ProcedureType, Integer> {

    Optional<ProcedureType> findByName(String name);

    boolean existsByName(String name);

    List<ProcedureType> findByNameContaining(String name);
}
