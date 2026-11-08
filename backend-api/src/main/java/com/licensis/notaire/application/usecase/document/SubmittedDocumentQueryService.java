package com.licensis.notaire.application.usecase.document;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.document.QuerySubmittedDocumentsUseCase;
import com.licensis.notaire.application.port.out.document.SubmittedDocumentCatalogPort;
import com.licensis.notaire.domain.document.SubmittedDocument;

@Service
@Transactional(readOnly = true)
public class SubmittedDocumentQueryService implements QuerySubmittedDocumentsUseCase {

    private final SubmittedDocumentCatalogPort catalog;

    public SubmittedDocumentQueryService(SubmittedDocumentCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<SubmittedDocument> findAll() {
        return catalog.findAll();
    }

    @Override
    public Optional<SubmittedDocument> findById(int id) {
        return catalog.findById(id);
    }
}
