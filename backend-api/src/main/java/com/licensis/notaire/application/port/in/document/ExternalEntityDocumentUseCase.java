package com.licensis.notaire.application.port.in.document;

import com.licensis.notaire.dto.DtoExternalEntityDocument;
import com.licensis.notaire.dto.DtoExternalEntityDocumentMovement;
import com.licensis.notaire.dto.DtoManagementExternalEntityDocuments;

/**
 * Inbound port for CU10 — external-entity documentation movements (issue #1866).
 */
public interface ExternalEntityDocumentUseCase {

    DtoManagementExternalEntityDocuments getDocuments(Integer idManagement);

    DtoExternalEntityDocument registerMovement(
            Integer idManagement, Integer idSubmittedDocument, DtoExternalEntityDocumentMovement movement);

    void tryCompleteDocumentation(Integer idManagement);
}
