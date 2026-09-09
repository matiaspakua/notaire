package com.licensis.notaire.api;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.service.DocumentCostTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API para administrar la plantilla de costos de documentos por tipo de trámite (CU27/CU39).
 */
@RestController
@RequestMapping("/api/v1/plantilla-costos-documento")
@Tag(name = "PlantillaCostosDocumento", description = "API para administrar costos esperados de documentos por tipo de trámite")
public class DocumentCostTemplateController {

    private final DocumentCostTemplateService documentCostTemplateService;

    public DocumentCostTemplateController(DocumentCostTemplateService documentCostTemplateService) {
        this.documentCostTemplateService = documentCostTemplateService;
    }

    public record CrearCostRequest(
            Integer idProcedureType,
            Integer idDocumentType,
            Float fixedAmount,
            Float variablePercentage) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Tipo de trámite o documento no encontrado")
    })
    @PostMapping
    @Operation(summary = "Definir el costo (fijo o variable) de un tipo de documento en la plantilla de un tipo de trámite")
    public ResponseEntity<Object> crearCost(@RequestBody CrearCostRequest request) {
        try {
            DocumentCostTemplate creado = documentCostTemplateService.crear(
                    request.idProcedureType(), request.idDocumentType(),
                    request.fixedAmount(), request.variablePercentage());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/tipo-tramite/{idProcedureType}")
    @Operation(summary = "Obtener los costos de documentos definidos para un tipo de trámite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentCostTemplate>> getCostosByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(documentCostTemplateService.findByTypeProcedure(idProcedureType));
    }
}
