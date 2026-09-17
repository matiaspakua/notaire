package com.licensis.notaire.application.port.out.document;

import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentCostTemplatePK;
import com.licensis.notaire.business.DocumentType;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for document persistence operations.
 */
public interface DocumentRepositoryPort {

    DocumentCostTemplate saveCostTemplate(DocumentCostTemplate entity);

    Optional<DocumentCostTemplate> findCostTemplateById(DocumentCostTemplatePK id);

    List<DocumentCostTemplate> findCostTemplatesByProcedureType(Integer procedureTypeId);

    Optional<DocumentType> findDocumentTypeById(Integer id);

    boolean existsCostTemplateById(DocumentCostTemplatePK id);

    void deleteCostTemplateById(DocumentCostTemplatePK id);
}
