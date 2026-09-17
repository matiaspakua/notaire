package com.licensis.notaire.adapter.out.persistence.document;

import com.licensis.notaire.application.port.out.document.ExternalDocumentRepositoryPort;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for external entity document operations.
 */
@Component
public class ExternalDocumentPersistenceAdapter implements ExternalDocumentRepositoryPort {

    private final SubmittedDocumentRepository submittedDocumentRepository;
    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;

    public ExternalDocumentPersistenceAdapter(SubmittedDocumentRepository submittedDocumentRepository,
                                               DeedManagementRepository managementRepository,
                                               ProcedureRepository procedureRepository) {
        this.submittedDocumentRepository = submittedDocumentRepository;
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
    }

    @Override
    public SubmittedDocument saveSubmittedDocument(SubmittedDocument entity) {
        return submittedDocumentRepository.save(entity);
    }

    @Override
    public Optional<SubmittedDocument> findSubmittedDocumentById(Integer id) {
        return submittedDocumentRepository.findById(id);
    }

    @Override
    public List<SubmittedDocument> findByManagementAndDeliveredBy(Integer managementId, String deliveredBy) {
        return submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
            managementId, deliveredBy);
    }

    @Override
    public Optional<DeedManagement> findManagementById(Integer id) {
        return managementRepository.findById(id);
    }

    @Override
    public List<Procedure> findProceduresByManagementId(Integer managementId) {
        return procedureRepository.findByFkIdManagementIdManagement(managementId);
    }
}
