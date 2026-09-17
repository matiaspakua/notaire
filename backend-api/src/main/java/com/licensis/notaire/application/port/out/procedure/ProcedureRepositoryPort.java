package com.licensis.notaire.application.port.out.procedure;

import com.licensis.notaire.business.Procedure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for procedure persistence operations.
 */
public interface ProcedureRepositoryPort {

    Page<Procedure> findAll(Pageable pageable);

    Optional<Procedure> findById(Integer id);

    Procedure save(Procedure entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Procedure> findByFkIdProcedureTypeIdProcedureType(Integer idProcedureType);
}
