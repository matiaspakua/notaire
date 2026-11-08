package com.licensis.notaire.application.port.in.document;

import java.util.List;

import com.licensis.notaire.domain.document.DocumentType;

public interface QueryDocumentTypesUseCase {

    List<DocumentType> findAll();

    List<DocumentType> searchByName(String text);

    /** @throws com.licensis.notaire.domain.document.DocumentTypeNotFoundException when absent */
    DocumentType getById(int id);

    /** @throws com.licensis.notaire.domain.document.DocumentTypeNotFoundException when absent */
    boolean isInUse(int id);
}
