package com.licensis.notaire.application.port.out.document;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.domain.document.SubmittedDocument;

/** Submitted document storage in domain terms (issue #1726). */
public interface SubmittedDocumentCatalogPort {

    List<SubmittedDocument> findAll();

    Optional<SubmittedDocument> findById(int id);

    /** Inserts when the document has no id, otherwise updates it. */
    SubmittedDocument save(SubmittedDocument document);

    boolean existsById(int id);

    /** @return false when no document had that id */
    boolean deleteById(int id);
}
