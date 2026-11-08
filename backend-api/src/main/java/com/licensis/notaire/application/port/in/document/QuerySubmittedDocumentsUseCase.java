package com.licensis.notaire.application.port.in.document;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.document.SubmittedDocument;

public interface QuerySubmittedDocumentsUseCase {

    List<SubmittedDocument> findAll();

    Optional<SubmittedDocument> findById(int id);
}
