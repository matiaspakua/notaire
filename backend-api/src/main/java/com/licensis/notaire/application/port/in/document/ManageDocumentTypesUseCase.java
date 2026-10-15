package com.licensis.notaire.application.port.in.document;

import com.licensis.notaire.domain.document.DocumentType;

public interface ManageDocumentTypesUseCase {

    /** {@code version} is the one the client read; creating ignores it, updating must match the stored one. */
    record SaveDocumentTypeCommand(
            String name,
            boolean expires,
            Integer dueDays,
            String deliveredBy,
            boolean enabled,
            boolean returned,
            int version) {}

    DocumentType create(SaveDocumentTypeCommand command);

    void update(int id, SaveDocumentTypeCommand command);

    void delete(int id);
}
