package com.licensis.notaire.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * CU43 - Elección del Gestor/Escribano: trámite y tipo de documento a
 * reingresar.
 */
public record DtoReingresoDocumentacionRequest(
        @NotNull @Schema(description = "Trámite de la gestión", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idProcedure,
        @NotNull @Schema(description = "Tipo de documento a reingresar", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer idDocumentType) {
}
