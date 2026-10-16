package com.licensis.notaire.application.port.in.workflow;

import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoDocumentReentryRequest;
import com.licensis.notaire.dto.DtoManagementDocumentReentry;

/**
 * Inbound port for CU43 document re-entry against a management (issue #1851).
 */
public interface DocumentReentryUseCase {

    DtoManagementDocumentReentry getRequiredDocumentation(Integer idManagement);

    DtoDocumentReentered reenter(Integer idManagement, DtoDocumentReentryRequest request);
}
