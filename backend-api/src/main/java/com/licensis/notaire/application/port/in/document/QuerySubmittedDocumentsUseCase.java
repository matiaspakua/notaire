package com.licensis.notaire.application.port.in.document;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.document.SubmittedDocumentCatalogPort.PageResult;
import com.licensis.notaire.domain.document.SubmittedDocument;

public interface QuerySubmittedDocumentsUseCase {

    List<SubmittedDocument> findAll();

    PageResult findPage(int page, int size, String sort);

    Optional<SubmittedDocument> findById(int id);
}
