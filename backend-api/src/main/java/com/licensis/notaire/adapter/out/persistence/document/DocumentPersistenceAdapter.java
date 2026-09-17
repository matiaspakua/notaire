package com.licensis.notaire.adapter.out.persistence.document;

import com.licensis.notaire.application.port.out.document.DocumentRepositoryPort;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentCostTemplatePK;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.repository.DocumentCostTemplateRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for document slice.
 */
@Component
public class DocumentPersistenceAdapter implements DocumentRepositoryPort {

    private final DocumentCostTemplateRepository costTemplateRepository;
    private final DocumentTypeRepository documentTypeRepository;

    public DocumentPersistenceAdapter(DocumentCostTemplateRepository costTemplateRepository,
                                       DocumentTypeRepository documentTypeRepository) {
        this.costTemplateRepository = costTemplateRepository;
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public DocumentCostTemplate saveCostTemplate(DocumentCostTemplate entity) {
        return costTemplateRepository.save(entity);
    }

    @Override
    public Optional<DocumentCostTemplate> findCostTemplateById(DocumentCostTemplatePK id) {
        return costTemplateRepository.findById(id);
    }

    @Override
    public List<DocumentCostTemplate> findCostTemplatesByProcedureType(Integer procedureTypeId) {
        return costTemplateRepository.findByProcedureTypeIdProcedureType(procedureTypeId);
    }

    @Override
    public Optional<DocumentType> findDocumentTypeById(Integer id) {
        return documentTypeRepository.findById(id);
    }

    @Override
    public boolean existsCostTemplateById(DocumentCostTemplatePK id) {
        return costTemplateRepository.existsById(id);
    }

    @Override
    public void deleteCostTemplateById(DocumentCostTemplatePK id) {
        costTemplateRepository.deleteById(id);
    }
}
