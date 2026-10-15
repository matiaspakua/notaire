package com.licensis.notaire.application.usecase.document;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.document.QueryDocumentTypesUseCase;
import com.licensis.notaire.application.port.out.document.DocumentTypeCatalogPort;
import com.licensis.notaire.application.port.out.document.DocumentTypeUsagePort;
import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeNotFoundException;

@Service
@Transactional(readOnly = true)
public class DocumentTypeQueryService implements QueryDocumentTypesUseCase {

    private final DocumentTypeCatalogPort catalog;
    private final DocumentTypeUsagePort usage;

    public DocumentTypeQueryService(DocumentTypeCatalogPort catalog, DocumentTypeUsagePort usage) {
        this.catalog = catalog;
        this.usage = usage;
    }

    @Override
    public List<DocumentType> findAll() {
        return catalog.findAll();
    }

    @Override
    public List<DocumentType> searchByName(String text) {
        return catalog.findByNameContaining(text);
    }

    @Override
    public DocumentType getById(int id) {
        return catalog.findById(id).orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    @Override
    public boolean isInUse(int id) {
        getById(id);
        return usage.isInUse(id);
    }
}
