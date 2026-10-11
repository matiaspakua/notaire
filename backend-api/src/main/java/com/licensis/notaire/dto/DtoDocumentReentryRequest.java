package com.licensis.notaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * CU43 - The clerk or notary's choice: which procedure and document type
 * to re-enter.
 */
public record DtoDocumentReentryRequest(
        @NotNull @Schema(description = "Procedure of the management", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idProcedure,
        @NotNull @Schema(description = "Document type to re-enter", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idDocumentType) {
}
