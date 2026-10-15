package com.licensis.notaire.adapter.out.persistence.document;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeUsagePort;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;
import com.licensis.notaire.domain.document.DocumentTypeReferencedException;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;

/**
 * Maps the legacy {@code business.DocumentType} JPA entity, which stays because submitted documents, the document
 * persistence adapter, case summary, report, reentry and required-documentation services still use it, to the pure
 * domain type (issue #1705).
 */
@Component
public class DocumentTypePersistenceAdapter implements DocumentTypeCatalogPort, DocumentTypeUsagePort {

    private final DocumentTypeRepository types;
    private final ProcedureTemplateRepository procedureTemplates;
    private final SubmittedDocumentRepository submittedDocuments;

    public DocumentTypePersistenceAdapter(
            DocumentTypeRepository types,
            ProcedureTemplateRepository procedureTemplates,
            SubmittedDocumentRepository submittedDocuments) {
        this.types = types;
        this.procedureTemplates = procedureTemplates;
        this.submittedDocuments = submittedDocuments;
    }

    @Override
    public List<DocumentType> findAll() {
        return types.findAll().stream().map(DocumentTypePersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<DocumentType> findByNameContaining(String text) {
        return types.findByNameContaining(text).stream().map(DocumentTypePersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<DocumentType> findById(int id) {
        return types.findById(id).map(DocumentTypePersistenceAdapter::toDomain);
    }

    @Override
    public DocumentType save(DocumentType type) {
        com.licensis.notaire.business.DocumentType entity = type.id() == null
                ? new com.licensis.notaire.business.DocumentType()
                : types.findById(type.id()).orElseThrow(() -> new DocumentTypeNotFoundException(type.id()));
        entity.setName(type.name());
        entity.setExpires(type.expires());
        entity.setDueDays(type.dueDays());
        entity.setDeliveredBy(type.deliveredBy() == null ? "" : type.deliveredBy());
        entity.setEnabled(type.enabled());
        entity.setReturned(type.returned());
        return toDomain(types.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        if (!types.existsById(id)) {
            return false;
        }
        try {
            types.deleteById(id);
            types.flush();
        } catch (DataIntegrityViolationException referenced) {
            throw new DocumentTypeReferencedException(referenced);
        }
        return true;
    }

    @Override
    public boolean isInUse(int id) {
        return !procedureTemplates.findByDocumentTypeIdDocumentType(id).isEmpty()
                || submittedDocuments.existsByDocumentTypeIdDocumentType(id);
    }

    private static DocumentType toDomain(com.licensis.notaire.business.DocumentType entity) {
        return DocumentType.restore(
                entity.getIdDocumentType(),
                entity.getName(),
                entity.getExpires(),
                entity.getDueDays(),
                entity.getDeliveredBy(),
                entity.getEnabled(),
                entity.getReturned(),
                entity.getVersion());
    }
}
