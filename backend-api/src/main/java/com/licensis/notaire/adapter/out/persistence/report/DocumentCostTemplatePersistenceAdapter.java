package com.licensis.notaire.adapter.out.persistence.report;

import com.licensis.notaire.application.port.out.report.DocumentCostTemplateRepositoryPort;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentCostTemplatePK;
import com.licensis.notaire.repository.DocumentCostTemplateRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the document cost template slice.
 */
@Component
public class DocumentCostTemplatePersistenceAdapter implements DocumentCostTemplateRepositoryPort {

    private final DocumentCostTemplateRepository documentCostTemplateRepository;

    public DocumentCostTemplatePersistenceAdapter(DocumentCostTemplateRepository documentCostTemplateRepository) {
        this.documentCostTemplateRepository = documentCostTemplateRepository;
    }

    @Override
    public List<DocumentCostTemplate> findAll() {
        return documentCostTemplateRepository.findAll();
    }

    @Override
    public Optional<DocumentCostTemplate> findById(DocumentCostTemplatePK id) {
        return documentCostTemplateRepository.findById(id);
    }

    @Override
    public DocumentCostTemplate save(DocumentCostTemplate entity) {
        return documentCostTemplateRepository.save(entity);
    }

    @Override
    public void deleteById(DocumentCostTemplatePK id) {
        documentCostTemplateRepository.deleteById(id);
    }

    @Override
    public boolean existsById(DocumentCostTemplatePK id) {
        return documentCostTemplateRepository.existsById(id);
    }

    @Override
    public List<DocumentCostTemplate> findByProcedureTypeIdProcedureType(Integer idProcedureType) {
        return documentCostTemplateRepository.findByProcedureTypeIdProcedureType(idProcedureType);
    }
}
