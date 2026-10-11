package com.licensis.notaire.adapter.in.web.document;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.adapter.in.web.support.DocumentTypeRef;
import com.licensis.notaire.adapter.in.web.support.ProcedureTypeRef;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.application.usecase.document.DocumentCostTemplateService;
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
 * REST API for managing the template of costos of documents by procedure type (CU27/CU39).
 */
@RestController
@RequestMapping("/api/v1/document-cost-templates")
@Tag(name = "PlantillaCostosDocumento", description = "API for managing costos esperados of documents by procedure type")
public class DocumentCostTemplateController {

    private final DocumentCostTemplateService documentCostTemplateService;

    public DocumentCostTemplateController(DocumentCostTemplateService documentCostTemplateService) {
        this.documentCostTemplateService = documentCostTemplateService;
    }

    public record CostTemplateKey(int fkIdProcedureType, int fkIdDocumentType) {
    }

    public record CostTemplateResponse(CostTemplateKey documentCostTemplatePK, ProcedureTypeRef procedureType,
            DocumentTypeRef documentType, java.math.BigDecimal fixedAmount,
            java.math.BigDecimal variablePercentage, int version) {
        public static CostTemplateResponse from(DocumentCostTemplate cost) {
            return new CostTemplateResponse(
                    new CostTemplateKey(cost.getDocumentCostTemplatePK().getFkIdProcedureType(),
                            cost.getDocumentCostTemplatePK().getFkIdDocumentType()),
                    ProcedureTypeRef.from(cost.getProcedureType()), DocumentTypeRef.from(cost.getDocumentType()),
                    cost.getFixedAmount(), cost.getVariablePercentage(), cost.getVersion());
        }
    }

    public record CreateCostRequest(
            Integer idProcedureType,
            Integer idDocumentType,
            java.math.BigDecimal fixedAmount,
            java.math.BigDecimal variablePercentage) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Procedure type or document not found")
    })
    @PostMapping
    @Operation(summary = "Define the costo (fijo or variable) of a document type in the template of a procedure type")
    public ResponseEntity<Object> createCost(@RequestBody CreateCostRequest request) {
        try {
            DocumentCostTemplate creado = documentCostTemplateService.create(
                    request.idProcedureType(), request.idDocumentType(),
                    request.fixedAmount(), request.variablePercentage());
            return ResponseEntity.status(HttpStatus.CREATED).body(CostTemplateResponse.from(creado));
        } catch (BusinessValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/procedure-types/{idProcedureType}")
    @Operation(summary = "Get the costos of documents definidos for a procedure type")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CostTemplateResponse>> getCostosByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(documentCostTemplateService.findByTypeProcedure(idProcedureType).stream()
                .map(CostTemplateResponse::from).toList());
    }
}
