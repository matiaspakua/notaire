package com.licensis.notaire.application.usecase.document;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.document.ManageSubmittedDocumentsUseCase;
import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.SubmittedDocumentCatalogPort;
import com.licensis.notaire.application.port.out.procedure.ProcedureLookupPort;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.SubmittedDocument;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Submitted document administration. Resolves document type and procedure through ports, applies due inheritance from
 * the type, and persists domain values (issue #1726).
 */
@Service
@Transactional
public class SubmittedDocumentManagementService implements ManageSubmittedDocumentsUseCase {

    private final SubmittedDocumentCatalogPort catalog;
    private final DocumentTypeCatalogPort documentTypes;
    private final ProcedureLookupPort procedures;

    public SubmittedDocumentManagementService(
            SubmittedDocumentCatalogPort catalog,
            DocumentTypeCatalogPort documentTypes,
            ProcedureLookupPort procedures) {
        this.catalog = catalog;
        this.documentTypes = documentTypes;
        this.procedures = procedures;
    }

    @Override
    public SubmittedDocument create(SaveSubmittedDocumentCommand command) {
        DocumentType type = requireType(command.typeId());
        requireProcedure(command.procedureId());
        SubmittedDocument draft = SubmittedDocument.createNew()
                .withDocumentType(type.id(), type.name())
                .withProcedureId(command.procedureId());
        draft = applyPatch(draft, command, type);
        if (command.refreshDue() || command.typeId() != null || command.dateEntry() != null
                || command.deliveredBy() != null) {
            draft = draft.applyDueFromType(type, command.deliveredBy());
        }
        return catalog.save(draft);
    }

    @Override
    public void update(int id, SaveSubmittedDocumentCommand command) {
        SubmittedDocument existing = catalog.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submitted document not found: " + id));
        DocumentType type = null;
        if (command.typeId() != null) {
            type = requireType(command.typeId());
        } else if (existing.documentTypeId() != null) {
            type = documentTypes.findById(existing.documentTypeId()).orElse(null);
        }
        if (command.procedureId() != null) {
            requireProcedure(command.procedureId());
        }
        SubmittedDocument updated = applyPatch(existing, command, type);
        if (command.refreshDue()) {
            updated = updated.applyDueFromType(type, command.deliveredBy());
        }
        catalog.save(updated);
    }

    @Override
    public boolean delete(int id) {
        return catalog.deleteById(id);
    }

    private SubmittedDocument applyPatch(
            SubmittedDocument current, SaveSubmittedDocumentCommand command, DocumentType resolvedType) {
        SubmittedDocument next = current;
        if (command.typeId() != null && resolvedType != null) {
            next = next.withDocumentType(resolvedType.id(), resolvedType.name());
        }
        if (command.procedureId() != null) {
            next = next.withProcedureId(command.procedureId());
        }
        if (command.dateEntry() != null) {
            next = next.withDateEntry(command.dateEntry());
        }
        if (command.delivered() != null) {
            next = next.withDelivered(command.delivered());
        }
        if (command.name() != null) {
            next = next.withName(command.name());
        }
        return next;
    }

    private DocumentType requireType(Integer typeId) {
        if (typeId == null) {
            throw new ResourceNotFoundException("Document type not found: null");
        }
        return documentTypes
                .findById(typeId)
                .orElseThrow(() -> new ResourceNotFoundException("Document type not found: " + typeId));
    }

    private void requireProcedure(Integer procedureId) {
        if (procedureId == null || !procedures.existsById(Integer.valueOf(procedureId))) {
            throw new ResourceNotFoundException("Procedure not found: " + procedureId);
        }
    }
}
