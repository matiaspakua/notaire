package com.licensis.notaire.adapter.in.web.document;

import com.licensis.notaire.adapter.in.web.support.DocumentTypeRef;
import com.licensis.notaire.adapter.in.web.support.ProcedureTypeRef;
import com.licensis.notaire.application.port.in.documentcosttemplate.ManageDocumentCostTemplatesUseCase;
import com.licensis.notaire.application.port.in.documentcosttemplate.QueryDocumentCostTemplatesUseCase;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CostTemplateView;
import com.licensis.notaire.application.port.out.documentcosttemplate.DocumentCostTemplateOpsPort.CreateResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API for managing expected document costs by procedure type (CU27/CU39).
 * Calls inbound ports only; entity access lives behind {@code DocumentCostTemplateOpsPort}
 * (issue #1821).
 */
@RestController
@RequestMapping("/api/v1/document-cost-templates")
@Tag(name = "Document cost template", description = "API for managing expected document costs by procedure type")
public class DocumentCostTemplateController {

    private final QueryDocumentCostTemplatesUseCase queryDocumentCostTemplates;
    private final ManageDocumentCostTemplatesUseCase manageDocumentCostTemplates;

    public DocumentCostTemplateController(
            QueryDocumentCostTemplatesUseCase queryDocumentCostTemplates,
            ManageDocumentCostTemplatesUseCase manageDocumentCostTemplates) {
        this.queryDocumentCostTemplates = queryDocumentCostTemplates;
        this.manageDocumentCostTemplates = manageDocumentCostTemplates;
    }

    public record CostTemplateKey(int fkIdProcedureType, int fkIdDocumentType) {
    }

    public record CostTemplateResponse(CostTemplateKey documentCostTemplatePK, ProcedureTypeRef procedureType,
            DocumentTypeRef documentType, java.math.BigDecimal fixedAmount,
            java.math.BigDecimal variablePercentage, int version) {
        public static CostTemplateResponse from(CostTemplateView view) {
            ProcedureTypeRef procedureType = view.procedureTypeRefId() == null
                    ? null
                    : new ProcedureTypeRef(
                            view.procedureTypeRefId(),
                            view.procedureTypeName(),
                            view.procedureTypeNotes(),
                            view.procedureTypeArchived(),
                            view.procedureTypeRegistered(),
                            view.procedureTypeAssociatesProperties(),
                            view.procedureTypeEnabled());
            DocumentTypeRef documentType = view.documentTypeRefId() == null
                    ? null
                    : new DocumentTypeRef(
                            view.documentTypeRefId(),
                            view.documentTypeName(),
                            view.documentTypeExpires(),
                            view.documentTypeDueDays(),
                            view.documentTypeDeliveredBy(),
                            view.documentTypeReturned(),
                            view.documentTypeEnabled());
            return new CostTemplateResponse(
                    new CostTemplateKey(view.procedureTypeId(), view.documentTypeId()),
                    procedureType,
                    documentType,
                    view.fixedAmount(),
                    view.variablePercentage(),
                    view.version());
        }
    }

    public record CreateCostRequest(
            @NotNull Integer idProcedureType,
            @NotNull Integer idDocumentType,
            java.math.BigDecimal fixedAmount,
            java.math.BigDecimal variablePercentage) {
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Procedure type or document not found")
    })
    @PostMapping
    @Operation(summary = "Define the fixed or variable cost of a document type in a procedure type template")
    public ResponseEntity<Object> createCost(@Valid @RequestBody CreateCostRequest request) {
        return switch (manageDocumentCostTemplates.create(
                request.idProcedureType(),
                request.idDocumentType(),
                request.fixedAmount(),
                request.variablePercentage())) {
            case CreateResult.Created created ->
                    ResponseEntity.status(HttpStatus.CREATED).body(CostTemplateResponse.from(created.view()));
            case CreateResult.Invalid invalid ->
                    ResponseEntity.badRequest().body(Map.of("message", invalid.message()));
            case CreateResult.NotFound notFound ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", notFound.message()));
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/procedure-types/{idProcedureType}")
    @Operation(summary = "Get the document costs defined for a procedure type")
    public ResponseEntity<List<CostTemplateResponse>> getCostosByTypeProcedure(
            @PathVariable Integer idProcedureType) {
        return ResponseEntity.ok(queryDocumentCostTemplates.findByProcedureType(idProcedureType).stream()
                .map(CostTemplateResponse::from)
                .toList());
    }
}
