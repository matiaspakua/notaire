package com.licensis.notaire.application.port.out.document;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.document.DocumentType;

/** Document type storage in domain terms. */
public interface DocumentTypeCatalogPort {

    List<DocumentType> findAll();

    List<DocumentType> findByNameContaining(String text);

    Optional<DocumentType> findById(int id);

    /** Inserts when the type has no id, otherwise updates it. */
    DocumentType save(DocumentType type);

    /**
     * @return false when no type had that id
     * @throws com.licensis.notaire.domain.document.DocumentTypeReferencedException when other records still
     *         reference it
     */
    boolean deleteById(int id);
}
