package com.licensis.notaire.adapter.in.web.management;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.licensis.notaire.application.port.in.management.ManageManagementsUseCase;
import com.licensis.notaire.application.port.in.management.QueryManagementsUseCase;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ArchiveResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementCommand;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.ManagementView;
import com.licensis.notaire.application.port.out.management.ManagementOpsPort.SaveResult;
import com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService;
import com.licensis.notaire.application.port.in.workflow.DocumentReentryUseCase;
import com.licensis.notaire.application.port.in.workflow.QueryWorkflowTraceUseCase;
import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoDocumentReentryRequest;
import com.licensis.notaire.dto.DtoExternalEntityDocument;
import com.licensis.notaire.dto.DtoExternalEntityDocumentMovement;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoManagementDocumentReentry;
import com.licensis.notaire.dto.DtoManagementExternalEntityDocuments;
import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Deed managements. Calls inbound ports only; orchestration and persistence live in the use cases
 * (issue #1772).
 */
@RestController
@RequestMapping("/api/v1/managements")
@Tag(name = "Managements", description = "API for deed managements")
public class ManagementController {

    private static final Logger log = LoggerFactory.getLogger(ManagementController.class);

    private final QueryManagementsUseCase queryManagements;
    private final ManageManagementsUseCase manageManagements;
    private final QueryWorkflowTraceUseCase workflowTrace;
    private final TransitionManagementUseCase transitionManagementUseCase;
    private final TransitionManagementWebMapper transitionManagementWebMapper;
    private final ExternalEntityDocumentService externalEntityDocumentService;
    private final DocumentReentryUseCase documentReentry;

    public ManagementController(
            QueryManagementsUseCase queryManagements,
            ManageManagementsUseCase manageManagements,
            QueryWorkflowTraceUseCase workflowTrace,
            TransitionManagementUseCase transitionManagementUseCase,
            TransitionManagementWebMapper transitionManagementWebMapper,
            ExternalEntityDocumentService externalEntityDocumentService,
            DocumentReentryUseCase documentReentry) {
        this.queryManagements = queryManagements;
        this.manageManagements = manageManagements;
        this.workflowTrace = workflowTrace;
        this.transitionManagementUseCase = transitionManagementUseCase;
        this.transitionManagementWebMapper = transitionManagementWebMapper;
        this.externalEntityDocumentService = externalEntityDocumentService;
        this.documentReentry = documentReentry;
    }

    public record DtoPendingBalance(java.math.BigDecimal pendingBalance) {}

    public record DtoArchivedManagement(Integer idManagement, java.math.BigDecimal pendingBalance,
            boolean pendingDebtAtArchiving) {}

    public record DtoTransitionRequest(String statusDestination) {}

    public record CompleteCaseRequest(Integer number, String heading, String notes,
            Integer budgetId, Integer notaryId, Integer statusManagementId, Integer typeProcedureId,
            Integer propertyId) {}

    /**
     * Issue #1360: current case values in the shape PUT /{id}/complete-case accepts, so an
     * edit form can prefill every field and send them back unchanged except what the user edits.
     */
    public record CompleteCaseResponse(Integer idManagement, Integer number, String heading,
            String notes, Integer budgetId, Integer notaryId, Integer statusManagementId,
            Integer typeProcedureId, Integer propertyId) {}

    record ManagementRequest(
            Integer number,
            @NotBlank String heading,
            Date dateStart,
            String notes,
            Integer notaryPersonId,
            Integer managementStatusId,
            Boolean pendingDebtAtArchiving) {}

    record ManagementResponse(
            Integer idManagement,
            int number,
            String heading,
            Date dateStart,
            String notes,
            Integer notaryPersonId,
            Integer managementStatusId,
            Boolean pendingDebtAtArchiving,
            int version) {}

    private static CompleteCaseCommand toCommand(CompleteCaseRequest request) {
        return new CompleteCaseCommand(
                request.number(), request.heading(), request.notes(), request.budgetId(),
                request.notaryId(), request.statusManagementId(), request.typeProcedureId(),
                request.propertyId());
    }

    private static ManagementCommand toCommand(ManagementRequest request) {
        return new ManagementCommand(
                request.number(), request.heading(), request.dateStart(), request.notes(),
                request.notaryPersonId(), request.managementStatusId(), request.pendingDebtAtArchiving());
    }

    private static ManagementResponse toResponse(ManagementView view) {
        return new ManagementResponse(
                view.idManagement(), view.number(), view.heading(), view.dateStart(), view.notes(),
                view.notaryPersonId(), view.managementStatusId(), view.pendingDebtAtArchiving(),
                view.version());
    }

    private static CompleteCaseResponse toResponse(CompleteCaseView view) {
        return new CompleteCaseResponse(
                view.idManagement(), view.number(), view.heading(), view.notes(), view.budgetId(),
                view.notaryId(), view.statusManagementId(), view.typeProcedureId(), view.propertyId());
    }

    @PostMapping("/complete-case")
    @Operation(summary = "CU02 - Create a management with its required dependencies",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 - When the procedure type has a workflow, the initial status must be a start node. "
                    + "CU13 - Initial status is appended to History.")
    public ResponseEntity<Object> createCompleteCase(@RequestBody CompleteCaseRequest request) {
        return switch (manageManagements.createCompleteCase(toCommand(request))) {
            case CompleteCaseResult.Created created -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(created.summary());
            case CompleteCaseResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case CompleteCaseResult.NotFound ignored -> ResponseEntity.notFound().build();
            case CompleteCaseResult.Updated ignored -> ResponseEntity.ok().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/complete-case")
    @Operation(summary = "CU53 - Get a management with its case dependencies, as PUT /{id}/complete-case accepts them",
            description = "Issue #1360 - Returns the management number, heading and notes plus the ids of its "
                    + "budget, notary, status, procedure type and property (null when none), taken from the "
                    + "same procedure PUT /{id}/complete-case updates. Lets the UI prefill a full edit.")
    public ResponseEntity<CompleteCaseResponse> getCompleteCase(@PathVariable Integer id) {
        return queryManagements.findCompleteCase(id)
                .map(view -> ResponseEntity.ok(toResponse(view)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete-case")
    @Operation(summary = "CU02/CU53 - Update a management with its required dependencies",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 **BREAKING** - Changing statusManagementId is rejected; use "
                    + "POST /{id}/transition. Same-status updates remain allowed.")
    public ResponseEntity<Object> updateCompleteCase(@PathVariable Integer id,
            @RequestBody CompleteCaseRequest request) {
        return switch (manageManagements.updateCompleteCase(id, toCommand(request))) {
            case CompleteCaseResult.Updated updated -> ResponseEntity.ok(updated.summary());
            case CompleteCaseResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case CompleteCaseResult.NotFound ignored -> ResponseEntity.notFound().build();
            case CompleteCaseResult.Created ignored -> ResponseEntity.status(HttpStatus.CREATED).build();
        };
    }

    @GetMapping
    @Operation(summary = "List all managements")
    public ResponseEntity<Page<DtoManagementSummary>> getAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        // Preserve Spring sort direction (e.g. idManagement,desc) for newest-first lists (#1340 / #1347).
        var order = pageable.getSort().stream().findFirst().orElse(null);
        String sort = order == null
                ? "idManagement,desc"
                : order.getProperty() + "," + order.getDirection().name().toLowerCase();
        var result = queryManagements.findAll(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return ResponseEntity.ok(new PageImpl<>(result.content(), pageable, result.totalElements()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get management by ID")
    public ResponseEntity<DtoManagementSummary> getById(@PathVariable Integer id) {
        return queryManagements.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "Get management by number")
    public ResponseEntity<DtoManagementSummary> getByNumber(@PathVariable Integer number) {
        return queryManagements.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{idPerson}")
    @Operation(summary = "CU19 - List managements for a client")
    public ResponseEntity<List<ManagementResponse>> getByClient(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(queryManagements.findByClientPersonId(idPerson).stream()
                .map(ManagementController::toResponse)
                .toList());
    }

    @GetMapping("/{id}/current-status")
    @Operation(summary = "CU13/CU14 - Get current management status",
            description = "Returns the latest History row by date when present; otherwise synthesizes a "
                    + "summary from DeedManagement.fkIdManagementStatus (as-of-now). Missing management "
                    + "or null status yields 404.")
    public ResponseEntity<DtoHistorySummary> getCurrentStatus(@PathVariable Integer id) {
        return queryManagements.findCurrentStatus(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "CU02 - Create a new management",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU13 - When a status is provided, the initial status is appended to History. "
                    + "Plain create has no procedure/workflow yet, so any defined status is accepted.")
    public ResponseEntity<Object> create(@Valid @RequestBody ManagementRequest request) {
        return switch (manageManagements.create(toCommand(request))) {
            case SaveResult.Created created -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(toResponse(created.view()));
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Status mutation rejected — use POST /{id}/transition"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @Operation(summary = "CU53 - Update a management",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 **BREAKING** - Changing managementStatusId is rejected; use "
                    + "POST /{id}/transition. Same-status updates remain allowed. "
                    + "CU13 - History for status changes is written by /transition (and create).")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody ManagementRequest request) {
        return switch (manageManagements.update(id, toCommand(request))) {
            case SaveResult.Updated ignored -> ResponseEntity.ok().build();
            case SaveResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case SaveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case SaveResult.ServerError ignored -> ResponseEntity.internalServerError().build();
            case SaveResult.Created ignored -> ResponseEntity.status(HttpStatus.CREATED).build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete management")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return switch (manageManagements.delete(id)) {
            case DeleteResult.Deleted ignored -> ResponseEntity.noContent().build();
            case DeleteResult.NotFound ignored -> ResponseEntity.notFound().build();
            case DeleteResult.Conflict ignored -> ResponseEntity.status(HttpStatus.CONFLICT).build();
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found"),
        @ApiResponse(responseCode = "400", description = "Management has no procedures or workflow definition")
    })
    @GetMapping("/{id}/workflow-trace")
    @Operation(summary = "Get workflow trace for a management (nodes, transitions, history, node statuses)",
            description = "CU83 legal-next contract: clients derive valid destination statuses from "
                    + "`transitions` whose `originNodeId` matches the current node (the node whose "
                    + "status equals `currentStatus`). The managements UI must offer only those "
                    + "destinations when changing status via POST /{id}/transition.")
    public ResponseEntity<Object> getWorkflowTrace(@PathVariable Integer id) {
        try {
            DtoManagementWorkflowTrace trace = workflowTrace.buildTrace(id);
            return ResponseEntity.ok(trace);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot build workflow trace for management {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to build workflow trace for management id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/outstanding-balance")
    @Operation(summary = "CU16 - Calculate aggregated pending balance for a management (RF-22)")
    public ResponseEntity<DtoPendingBalance> getPendingBalance(@PathVariable Integer id) {
        try {
            java.math.BigDecimal pendingBalance = queryManagements.calculatePendingBalance(id);
            return ResponseEntity.ok(new DtoPendingBalance(pendingBalance));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/financial-summary")
    @Operation(summary = "CU47/CU02 - Get aggregated financial summary for a management")
    public ResponseEntity<DtoManagementFinancialSummary> getFinancialSummary(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(queryManagements.getFinancialSummary(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Management archived"),
        @ApiResponse(responseCode = "404", description = "Management not found"),
        @ApiResponse(responseCode = "409",
                description = "Unresolved waiting procedure folders (CU85); confirm to archive")
    })
    @PostMapping("/{id}/archive")
    @Operation(summary = "CU16 - Archive a management, warning and recording pending debt (RF-22, RF-37); "
            + "cascades to CU85 procedure folders")
    public ResponseEntity<Object> archiving(@PathVariable Integer id,
            @RequestParam(name = "confirmado", defaultValue = "false") boolean confirmed) {
        return switch (manageManagements.archive(id, confirmed)) {
            case ArchiveResult.Ok ok -> ResponseEntity.ok(new DtoArchivedManagement(
                    ok.view().idManagement(), ok.view().pendingBalance(),
                    ok.view().pendingDebtAtArchiving()));
            case ArchiveResult.NotFound ignored -> ResponseEntity.notFound().build();
            case ArchiveResult.WaitingFolders waiting -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", waiting.message(),
                            "waitingFolderNumbers", waiting.waitingFolderNumbers()));
        };
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transition applied"),
        @ApiResponse(responseCode = "400", description = "Transition not valid for the procedure-type workflow"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @PostMapping("/{id}/transition")
    @Operation(summary = "CU83 - Transition management status validating the defined workflow")
    public ResponseEntity<?> transition(@PathVariable Integer id,
            @RequestBody DtoTransitionRequest request) {
        var output = transitionManagementUseCase.execute(id, request.statusDestination());
        return ResponseEntity.ok(transitionManagementWebMapper.mapToHttpResponse(output));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/history")
    @Operation(summary = "CU13 - Get the full management History log, ordered chronologically")
    public ResponseEntity<List<DtoHistorySummary>> getHistory(@PathVariable Integer id) {
        return queryManagements.findHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/external-entity-documents")
    @Operation(summary = "CU10 - Get management documentation handled by external entities")
    public ResponseEntity<DtoManagementExternalEntityDocuments> getExternalEntityDocuments(
            @PathVariable Integer id) {
        return ResponseEntity.ok(externalEntityDocumentService.getDocuments(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movement registered"),
        @ApiResponse(responseCode = "400", description = "Invalid document for the management"),
        @ApiResponse(responseCode = "404", description = "Management or document not found")
    })
    @PutMapping("/{id}/external-entity-documents/{idSubmittedDocument}")
    @Operation(summary = "CU10 - Register movement of an external-entity document")
    public ResponseEntity<DtoExternalEntityDocument> registerExternalEntityDocumentMovement(
            @PathVariable Integer id, @PathVariable Integer idSubmittedDocument,
            @RequestBody DtoExternalEntityDocumentMovement movement) {
        DtoExternalEntityDocument result =
                externalEntityDocumentService.registerMovement(id, idSubmittedDocument, movement);
        externalEntityDocumentService.tryCompleteDocumentation(id);
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/document-reentry")
    @Operation(summary = "CU43 - Get management procedures with required documentation")
    public ResponseEntity<DtoManagementDocumentReentry> getRequiredDocumentationForReentry(
            @PathVariable Integer id) {
        return ResponseEntity.ok(documentReentry.getRequiredDocumentation(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Document re-entered"),
        @ApiResponse(responseCode = "400", description = "Invalid procedure or document type"),
        @ApiResponse(responseCode = "404", description = "Management or procedure not found")
    })
    @PostMapping("/{id}/document-reentry")
    @Operation(summary = "CU43 - Re-enter a document type for a management procedure")
    public ResponseEntity<DtoDocumentReentered> reenterDocumentation(@PathVariable Integer id,
            @Valid @RequestBody DtoDocumentReentryRequest request) {
        DtoDocumentReentered result = documentReentry.reenter(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
