package com.licensis.notaire.application.port.out.report;

import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentCostTemplatePK;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for document cost template persistence operations.
 */
public interface DocumentCostTemplateRepositoryPort {

    List<DocumentCostTemplate> findAll();

    Optional<DocumentCostTemplate> findById(DocumentCostTemplatePK id);

    DocumentCostTemplate save(DocumentCostTemplate entity);

    void deleteById(DocumentCostTemplatePK id);

    boolean existsById(DocumentCostTemplatePK id);

    List<DocumentCostTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType);
}
