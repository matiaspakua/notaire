package com.licensis.notaire.application.port.in.document;

import java.time.LocalDate;

import com.licensis.notaire.domain.document.SubmittedDocument;

public interface ManageSubmittedDocumentsUseCase {

    /**
     * Partial update fields: null means "leave unchanged" (except create, where typeId and procedureId are required
     * by the wire contract before this command is built).
     */
    record SaveSubmittedDocumentCommand(
            Integer typeId,
            LocalDate dateEntry,
            Boolean delivered,
            Integer procedureId,
            String deliveredBy,
            String name,
            boolean refreshDue) {}

    SubmittedDocument create(SaveSubmittedDocumentCommand command);

    void update(int id, SaveSubmittedDocumentCommand command);

    /** @return false when no row had that id */
    boolean delete(int id);
}
