package com.licensis.notaire.adapter.out.persistence.document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.document.SubmittedDocumentCatalogPort;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.domain.document.SubmittedDocument;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;

/**
 * Maps the legacy {@code business.SubmittedDocument} JPA entity (kept for external-entity, reports, and other
 * consumers) to the pure domain type (issue #1726).
 */
@Component
public class SubmittedDocumentPersistenceAdapter implements SubmittedDocumentCatalogPort {

    private final SubmittedDocumentRepository documents;
    private final DocumentTypeRepository documentTypes;
    private final ProcedureRepository procedures;

    public SubmittedDocumentPersistenceAdapter(
            SubmittedDocumentRepository documents,
            DocumentTypeRepository documentTypes,
            ProcedureRepository procedures) {
        this.documents = documents;
        this.documentTypes = documentTypes;
        this.procedures = procedures;
    }

    @Override
    public List<SubmittedDocument> findAll() {
        return documents.findAll().stream().map(SubmittedDocumentPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<SubmittedDocument> findById(int id) {
        return documents.findById(id).map(SubmittedDocumentPersistenceAdapter::toDomain);
    }

    @Override
    public SubmittedDocument save(SubmittedDocument document) {
        com.licensis.notaire.business.SubmittedDocument entity = document.id() == null
                ? newEntity()
                : documents
                        .findById(document.id())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Submitted document not found: " + document.id()));
        applyDomain(entity, document);
        return toDomain(documents.save(entity));
    }

    @Override
    public boolean existsById(int id) {
        return documents.existsById(id);
    }

    @Override
    public boolean deleteById(int id) {
        if (!documents.existsById(id)) {
            return false;
        }
        documents.deleteById(id);
        return true;
    }

    private void applyDomain(com.licensis.notaire.business.SubmittedDocument entity, SubmittedDocument document) {
        entity.setName(document.name() == null ? "" : document.name());
        entity.setDelivered(document.delivered() != null && document.delivered());
        entity.setDeliveredBy(document.deliveredBy() == null ? "" : document.deliveredBy());
        entity.setExpires(document.expires());
        entity.setDueDays(document.dueDays());
        entity.setPrepared(document.prepared());
        entity.setReleased(document.released());
        entity.setFlagged(document.flagged());
        entity.setReentered(document.reentered());
        entity.setDateEntry(document.dateEntry() == null ? null : toDate(document.dateEntry()));
        entity.setDateDue(document.dateDue() == null ? null : toDate(document.dateDue()));

        if (document.documentTypeId() != null) {
            DocumentType type = documentTypes
                    .findById(document.documentTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Document type not found: " + document.documentTypeId()));
            entity.setDocumentType(type);
        }
        if (document.procedureId() != null) {
            Procedure procedure = procedures
                    .findById(document.procedureId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Procedure not found: " + document.procedureId()));
            entity.setFkIdProcedure(procedure);
        }
    }

    private static com.licensis.notaire.business.SubmittedDocument newEntity() {
        com.licensis.notaire.business.SubmittedDocument entity = new com.licensis.notaire.business.SubmittedDocument();
        entity.setDelivered(false);
        entity.setName("");
        entity.setPrepared(false);
        entity.setReleased(false);
        entity.setFlagged(false);
        entity.setReentered(false);
        entity.setDeliveredBy("");
        return entity;
    }

    private static SubmittedDocument toDomain(com.licensis.notaire.business.SubmittedDocument entity) {
        DocumentType type = entity.getDocumentType();
        Integer typeId = type != null ? type.getIdDocumentType() : entity.getFkIdDocumentTypeNullable();
        String typeName = type != null ? type.getName() : null;
        Integer procedureId =
                entity.getFkIdProcedure() != null ? entity.getFkIdProcedure().getIdProcedure() : null;
        return SubmittedDocument.restore(
                entity.getIdSubmittedDocument(),
                typeId,
                typeName,
                entity.getDateEntry() == null ? null : toLocalDate(entity.getDateEntry()),
                entity.getDelivered(),
                procedureId,
                entity.getName(),
                entity.getDeliveredBy(),
                entity.getExpires(),
                entity.getDueDays(),
                entity.getDateDue() == null ? null : toLocalDate(entity.getDateDue()),
                entity.getPrepared(),
                Boolean.TRUE.equals(entity.getReleased()),
                Boolean.TRUE.equals(entity.getFlagged()),
                Boolean.TRUE.equals(entity.getReentered()));
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
