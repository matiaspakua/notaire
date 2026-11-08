package com.licensis.notaire.application.port.in.procedure;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.procedure.ProcedureTemplateOpsPort.TemplateView;

/**
 * Inbound query port for procedure templates (issue #1798).
 */
public interface QueryProcedureTemplatesUseCase {

    List<TemplateView> findAll();

    List<TemplateView> findByProcedureType(Integer procedureTypeId);

    Optional<TemplateView> findById(Integer procedureTypeId, Integer documentTypeId);
}
