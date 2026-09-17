package com.licensis.notaire.application.port.out.procedure;

import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for procedure template persistence operations.
 */
public interface ProcedureTemplateRepositoryPort {

    List<ProcedureTemplate> findAll();

    Optional<ProcedureTemplate> findById(ProcedureTemplatePK id);

    ProcedureTemplate save(ProcedureTemplate entity);

    void deleteById(ProcedureTemplatePK id);

    boolean existsById(ProcedureTemplatePK id);

    List<ProcedureTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);
}
