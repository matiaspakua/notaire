package com.licensis.notaire.application.usecase.procedure;

import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Issue #773 (CU02/CU03) - When a procedure is created on case confirmation, materializes one
 * pending {@code SubmittedDocument} per document type required by the procedure type's
 * {@code ProcedureTemplate}. Idempotent on (procedure, document type).
 */
@Service
public class RequiredDocumentationOnConfirmService {

    private final ProcedureTemplateRepository procedureTemplateRepository;
    private final SubmittedDocumentRepository submittedDocumentRepository;

    public RequiredDocumentationOnConfirmService(ProcedureTemplateRepository procedureTemplateRepository,
            SubmittedDocumentRepository submittedDocumentRepository) {
        this.procedureTemplateRepository = procedureTemplateRepository;
        this.submittedDocumentRepository = submittedDocumentRepository;
    }

    /**
     * @return the number of pending documents created
     */
    @Transactional
    public int seedRequiredDocuments(Procedure procedure) {
        if (procedure.getFkIdProcedureType() == null) {
            return 0;
        }
        var templates = procedureTemplateRepository
                .findByProcedureTypeIdProcedureType(procedure.getFkIdProcedureType().getIdProcedureType());
        if (templates.isEmpty()) {
            return 0;
        }
        Set<Integer> existingTypeIds = new HashSet<>();
        for (SubmittedDocument existing : submittedDocumentRepository
                .findByFkIdProcedureIdProcedure(procedure.getIdProcedure())) {
            existingTypeIds.add(existing.getFkIdDocumentTypeNullable());
        }
        int created = 0;
        for (ProcedureTemplate template : templates) {
            DocumentType documentType = template.getDocumentType();
            if (existingTypeIds.add(documentType.getIdDocumentType())) {
                submittedDocumentRepository.save(pendingDocument(procedure, documentType));
                created++;
            }
        }
        return created;
    }

    private static SubmittedDocument pendingDocument(Procedure procedure, DocumentType documentType) {
        SubmittedDocument document = new SubmittedDocument();
        document.setFkIdProcedure(procedure);
        document.setDocumentType(documentType);
        document.setName(documentType.getName());
        document.setExpires(documentType.getExpires());
        document.setDueDays(documentType.getDueDays());
        document.setDeliveredBy(documentType.getDeliveredBy());
        document.setDelivered(false);
        document.setReentered(false);
        document.setReleased(false);
        document.setFlagged(false);
        return document;
    }
}
