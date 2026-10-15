package com.licensis.notaire.application.usecase.document;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.document.ManageDocumentTypesUseCase;
import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeUsagePort;
import com.licensis.notaire.application.usecase.catalog.CatalogLockSupport;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeInUseException;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;
import com.licensis.notaire.domain.document.DocumentTypeStaleVersionException;

/**
 * Document type administration. A type that procedure templates or submitted documents use is locked, because
 * changing it would rewrite what those records mean: it can be neither edited nor deleted, and a new type is created
 * instead.
 */
@Service
@Transactional
public class DocumentTypeManagementService implements ManageDocumentTypesUseCase {

    private final DocumentTypeCatalogPort catalog;
    private final DocumentTypeUsagePort usage;

    public DocumentTypeManagementService(DocumentTypeCatalogPort catalog, DocumentTypeUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public DocumentType create(SaveDocumentTypeCommand command) {
        return catalog.save(DocumentType.create(
                command.name(),
                command.expires(),
                command.dueDays(),
                command.deliveredBy(),
                command.enabled(),
                command.returned()));
    }

    @Override
    public void update(int id, SaveDocumentTypeCommand command) {
        CatalogLockSupport.update(
                id,
                catalog::findById,
                usage::isInUse,
                DocumentType::version,
                command.version(),
                () -> new DocumentTypeNotFoundException(id),
                DocumentTypeInUseException::lockedForEdit,
                DocumentTypeStaleVersionException::new,
                existing -> existing.changeTo(
                        command.name(),
                        command.expires(),
                        command.dueDays(),
                        command.deliveredBy(),
                        command.enabled(),
                        command.returned()),
                catalog::save);
    }

    @Override
    public void delete(int id) {
        CatalogLockSupport.delete(
                id,
                catalog::findById,
                usage::isInUse,
                () -> new DocumentTypeNotFoundException(id),
                DocumentTypeInUseException::cannotDelete,
                catalog::deleteById);
    }
}
