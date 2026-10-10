package com.licensis.notaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * CU43 - Elección of the Gestor/Notary: procedure and document type a
 * reingresar.
 */
public record DtoDocumentReentryRequest(
        @NotNull @Schema(description = "Procedure of the management", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idProcedure,
        @NotNull @Schema(description = "Tipo de documento a reingresar", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idDocumentType) {
}
