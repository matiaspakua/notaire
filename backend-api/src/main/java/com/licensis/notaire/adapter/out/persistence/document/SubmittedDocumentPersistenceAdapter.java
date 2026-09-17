package com.licensis.notaire.adapter.out.persistence.document;

import com.licensis.notaire.application.port.out.document.SubmittedDocumentRepositoryPort;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for submitted document operations.
 */
@Component
public class SubmittedDocumentPersistenceAdapter implements SubmittedDocumentRepositoryPort {

    private final SubmittedDocumentRepository repository;

    public SubmittedDocumentPersistenceAdapter(SubmittedDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public SubmittedDocument save(SubmittedDocument entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<SubmittedDocument> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<SubmittedDocument> findByManagementAndDeliveredBy(Integer managementId, String deliveredBy) {
        return repository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(managementId,
            deliveredBy);
    }

    @Override
    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }
}
