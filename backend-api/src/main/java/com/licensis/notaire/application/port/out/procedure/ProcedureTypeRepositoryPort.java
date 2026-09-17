package com.licensis.notaire.application.port.out.procedure;

import com.licensis.notaire.business.ProcedureType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for procedure type persistence operations.
 */
public interface ProcedureTypeRepositoryPort {

    List<ProcedureType> findAll();

    Optional<ProcedureType> findById(Integer id);

    ProcedureType save(ProcedureType entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<ProcedureType> findByNameContaining(String name);
}
