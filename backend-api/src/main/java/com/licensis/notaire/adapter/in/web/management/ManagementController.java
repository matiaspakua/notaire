package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.dto.DtoExternalEntityDocument;
import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementExternalEntityDocuments;
import com.licensis.notaire.dto.DtoManagementDocumentReentry;
import com.licensis.notaire.dto.DtoManagementFinancialSummary;
import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoExternalEntityDocumentMovement;
import com.licensis.notaire.dto.DtoDocumentReentryRequest;
import com.licensis.notaire.exception.FoldersInWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.HistoryOrder;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.PropertyRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.application.usecase.procedure.ProcedureFolderService;
import com.licensis.notaire.application.usecase.procedure.RequiredDocumentationOnConfirmService;
import com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService;
import com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService;
import com.licensis.notaire.application.usecase.management.ManagementStatusLogService;
import com.licensis.notaire.application.usecase.management.ManagementQueryService;
import com.licensis.notaire.application.usecase.management.ManagementFinancialSummaryService;
import com.licensis.notaire.application.usecase.management.ManagementStatusWriteGuard;
import com.licensis.notaire.application.usecase.management.ManagementSubstitutionService;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.application.usecase.workflow.DocumentReentryService;
import com.licensis.notaire.application.usecase.workflow.WorkflowTraceService;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/managements")
@Tag(name = "Managements", description = "API for deed managements")
public class ManagementController {

    private static final Logger log = LoggerFactory.getLogger(ManagementController.class);

    private final DeedManagementRepository repository;
    private final HistoryRepository historyRepository;
    private final WorkflowTraceService workflowTraceService;
    private final ManagementQueryService managementQueryService;
    private final PersonRepository personRepository;
    private final ManagementStatusRepository statusRepository;
    private final BudgetRepository budgetRepository;
    private final ProcedureTypeRepository typeProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final PropertyRepository propertyRepository;
    private final ManagementArchiveDebtService managementArchiveDebtService;
    private final ManagementSubstitutionService managementSubstitutionService;
    private final ManagementFinancialSummaryService financialSummaryService;
    private final ManagementStatusLogService statusLogService;
    private final ManagementStatusWriteGuard managementStatusWriteGuard;
    private final TransitionManagementUseCase transitionManagementUseCase;
    private final TransitionManagementWebMapper transitionManagementWebMapper;
    private final ExternalEntityDocumentService externalEntityDocumentService;
    private final DocumentReentryService documentReentryService;
    private final ProcedureFolderService procedureFolderService;
    private final RequiredDocumentationOnConfirmService requiredDocumentationOnConfirmService;

    public ManagementController(DeedManagementRepository repository,
                             HistoryRepository historyRepository,
                             WorkflowTraceService workflowTraceService,
                             ManagementQueryService managementQueryService, PersonRepository personRepository,
                             ManagementStatusRepository statusRepository, BudgetRepository budgetRepository,
                             ProcedureTypeRepository typeProcedureRepository, ProcedureRepository procedureRepository,
                             PropertyRepository propertyRepository,
                             ManagementArchiveDebtService managementArchiveDebtService,
                             ManagementSubstitutionService managementSubstitutionService,
                             ManagementFinancialSummaryService financialSummaryService,
                             ManagementStatusLogService statusLogService,
                             ManagementStatusWriteGuard managementStatusWriteGuard,
                             TransitionManagementUseCase transitionManagementUseCase,
                             TransitionManagementWebMapper transitionManagementWebMapper,
                             ExternalEntityDocumentService externalEntityDocumentService,
                             DocumentReentryService documentReentryService,
                             ProcedureFolderService procedureFolderService,
                             RequiredDocumentationOnConfirmService requiredDocumentationOnConfirmService) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.workflowTraceService = workflowTraceService;
        this.managementQueryService = managementQueryService;
        this.personRepository = personRepository;
        this.statusRepository = statusRepository;
        this.budgetRepository = budgetRepository;
        this.typeProcedureRepository = typeProcedureRepository;
        this.procedureRepository = procedureRepository;
        this.propertyRepository = propertyRepository;
        this.managementArchiveDebtService = managementArchiveDebtService;
        this.managementSubstitutionService = managementSubstitutionService;
        this.financialSummaryService = financialSummaryService;
        this.statusLogService = statusLogService;
        this.managementStatusWriteGuard = managementStatusWriteGuard;
        this.transitionManagementUseCase = transitionManagementUseCase;
        this.transitionManagementWebMapper = transitionManagementWebMapper;
        this.externalEntityDocumentService = externalEntityDocumentService;
        this.documentReentryService = documentReentryService;
        this.procedureFolderService = procedureFolderService;
        this.requiredDocumentationOnConfirmService = requiredDocumentationOnConfirmService;
    }

    public record DtoPendingBalance(java.math.BigDecimal pendingBalance) {}

    public record DtoArchivedManagement(Integer idManagement, java.math.BigDecimal pendingBalance, boolean pendingDebtAtArchiving) {}

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

    private record CaseDependencies(Budget budget, Person notary, ManagementStatus status,
            ProcedureType typeProcedure, Property property) {}

    private static boolean hasRequiredFields(CompleteCaseRequest request) {
        return request.number() != null && request.budgetId() != null && request.notaryId() != null
                && request.statusManagementId() != null && request.typeProcedureId() != null;
    }

    private Optional<CaseDependencies> resolveDependencies(CompleteCaseRequest request) {
        Optional<Budget> budget = budgetRepository.findById(request.budgetId());
        Optional<Person> notary = personRepository.findById(request.notaryId());
        Optional<ManagementStatus> status = statusRepository.findById(request.statusManagementId());
        Optional<ProcedureType> typeProcedure = typeProcedureRepository.findById(request.typeProcedureId());
        if (budget.isEmpty() || notary.isEmpty() || status.isEmpty() || typeProcedure.isEmpty()) {
            return Optional.empty();
        }
        Property property = null;
        if (request.propertyId() != null) {
            Optional<Property> found = propertyRepository.findById(request.propertyId());
            if (found.isEmpty()) {
                return Optional.empty();
            }
            property = found.get();
        }
        return Optional.of(new CaseDependencies(budget.get(), notary.get(), status.get(),
                typeProcedure.get(), property));
    }

    private void applyManagementFields(DeedManagement management, CompleteCaseRequest request,
            CaseDependencies dependencies) {
        management.setNumber(request.number());
        management.setHeading(request.heading() == null ? "Management" : request.heading());
        ManagementSubstitutionService.AssignedNotary assigned =
                managementSubstitutionService.resolveNotary(dependencies.notary(), management.getDateStart());
        management.setFkIdNotaryPerson(assigned.notary());
        management.setNotes(buildNotes(request.notes(), dependencies.notary(), assigned));
        management.setFkIdManagementStatus(dependencies.status());
    }

    private String buildNotes(String requestNotes, Person requestedNotary,
            ManagementSubstitutionService.AssignedNotary assigned) {
        if (assigned.appliedSubstitution() == null) {
            return requestNotes;
        }
        String redirectionNote = managementSubstitutionService.redirectionNote(
                requestedNotary, assigned.notary());
        if (requestNotes == null || requestNotes.isBlank()) {
            return redirectionNote;
        }
        return requestNotes + " | " + redirectionNote;
    }

    private void saveProcedure(DeedManagement management, CaseDependencies dependencies) {
        Procedure procedure = new Procedure();
        procedure.setFkIdManagement(management);
        applyProcedureDependencies(procedure, dependencies);
        Procedure saved = procedureRepository.save(procedure);
        procedureFolderService.generateFolderForProcedure(saved);
        requiredDocumentationOnConfirmService.seedRequiredDocuments(saved);
    }

    private void updateProcedure(Procedure procedure, CaseDependencies dependencies) {
        applyProcedureDependencies(procedure, dependencies);
        procedureRepository.save(procedure);
    }

    private static void applyProcedureDependencies(Procedure procedure, CaseDependencies dependencies) {
        procedure.setFkIdBudget(dependencies.budget());
        procedure.setFkIdProcedureType(dependencies.typeProcedure());
        if (dependencies.property() != null) {
            procedure.setFkIdProperty(dependencies.property());
        }
    }

    @PostMapping("/complete-case")
    @Transactional
    @Operation(summary = "CU02 - Create a management with its required dependencies",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 - When the procedure type has a workflow, the initial status must be a start node. "
                    + "CU13 - Initial status is appended to History.")
    public ResponseEntity<Object> createCompleteCase(@RequestBody CompleteCaseRequest request) {
        if (!hasRequiredFields(request)) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CaseDependencies deps = dependencies.get();
        managementStatusWriteGuard.validateInitialStatus(
                deps.typeProcedure().getWorkflowDefinition(), deps.status());
        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        applyManagementFields(management, request, deps);
        management = repository.save(management);
        saveProcedure(management, deps);
        statusLogService.registerStatus(management, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/complete-case")
    @Transactional(readOnly = true)
    @Operation(summary = "CU53 - Get a management with its case dependencies, as PUT /{id}/complete-case accepts them",
            description = "Issue #1360 - Returns the management number, heading and notes plus the ids of its "
                    + "budget, notary, status, procedure type and property (null when none), taken from the "
                    + "same procedure PUT /{id}/complete-case updates. Lets the UI prefill a full edit.")
    public ResponseEntity<CompleteCaseResponse> getCompleteCase(@PathVariable Integer id) {
        Optional<DeedManagement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DeedManagement management = existing.get();
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(id);
        Procedure procedure = procedures.isEmpty() ? null : procedures.get(0);
        return ResponseEntity.ok(new CompleteCaseResponse(
                management.getIdManagement(),
                management.getNumber(),
                management.getHeading(),
                management.getNotes(),
                procedure == null || procedure.getFkIdBudget() == null ? null
                        : procedure.getFkIdBudget().getIdBudget(),
                management.getFkIdNotaryPerson() == null ? null : management.getFkIdNotaryPerson().getPersonId(),
                statusIdOf(management),
                procedure == null || procedure.getFkIdProcedureType() == null ? null
                        : procedure.getFkIdProcedureType().getIdProcedureType(),
                procedure == null || procedure.getFkIdProperty() == null ? null
                        : procedure.getFkIdProperty().getIdProperty()));
    }

    @PutMapping("/{id}/complete-case")
    @Transactional
    @Operation(summary = "CU02/CU53 - Update a management with its required dependencies",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 **BREAKING** - Changing statusManagementId is rejected; use "
                    + "POST /{id}/transition. Same-status updates remain allowed.")
    public ResponseEntity<Object> updateCompleteCase(@PathVariable Integer id,
            @RequestBody CompleteCaseRequest request) {
        Optional<DeedManagement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!hasRequiredFields(request)) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DeedManagement management = existing.get();
        Integer previousStatusId = statusIdOf(management);
        managementStatusWriteGuard.rejectStatusMutationOnUpdate(
                previousStatusId, request.statusManagementId());
        CaseDependencies deps = dependencies.get();
        applyManagementFields(management, request, deps);
        management = repository.save(management);
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(id);
        if (procedures.isEmpty()) {
            saveProcedure(management, deps);
        } else {
            updateProcedure(procedures.get(0), deps);
        }
        // Status mutations are rejected above; History for status changes remains on /transition.
        registerStatusChangeIfNeeded(management, previousStatusId);
        return ResponseEntity.ok(managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @GetMapping
    @Operation(summary = "List all managements")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DtoManagementSummary>> getAll(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(managementQueryService.findAll(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get management by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getById(@PathVariable Integer id) {
        return managementQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    @Operation(summary = "Get management by number")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getByNumber(@PathVariable Integer number) {
        return managementQueryService.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{idPerson}")
    @Operation(summary = "CU19 - List managements for a client")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ManagementResponse>> getByClient(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(repository.findByClientPersonId(idPerson).stream()
                .map(this::toManagementResponse).toList());
    }

    @GetMapping("/{id}/current-status")
    @Operation(summary = "CU13/CU14 - Get current management status",
            description = "Returns the latest History row by date when present; otherwise synthesizes a "
                    + "summary from DeedManagement.fkIdManagementStatus (as-of-now). Missing management "
                    + "or null status yields 404.")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorySummary> getCurrentStatus(@PathVariable Integer id) {
        Optional<DeedManagement> managementOpt = repository.findById(id);
        if (managementOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<History> historyRows = historyRepository.findByFkIdManagementIdManagement(id);
        if (!historyRows.isEmpty()) {
            return historyRows.stream()
                    .max(HistoryOrder.CHRONOLOGICAL)
                    .map(h -> ResponseEntity.ok(
                            com.licensis.notaire.application.usecase.history.HistoryMapper.toDto(h)))
                    .orElse(ResponseEntity.notFound().build());
        }
        DeedManagement management = managementOpt.get();
        ManagementStatus status = management.getFkIdManagementStatus();
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new DtoHistorySummary(
                null,
                new Date(),
                null,
                management.getIdManagement(),
                status.getIdManagementStatus(),
                status.getName()));
    }

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

    private ManagementResponse toManagementResponse(DeedManagement entity) {
        Integer notaryId = entity.getFkIdNotaryPerson() != null
                ? entity.getFkIdNotaryPerson().getPersonId() : null;
        Integer statusId = entity.getFkIdManagementStatus() != null
                ? entity.getFkIdManagementStatus().getIdManagementStatus() : null;
        return new ManagementResponse(
                entity.getIdManagement(),
                entity.getNumber(),
                entity.getHeading(),
                entity.getDateStart(),
                entity.getNotes(),
                notaryId,
                statusId,
                entity.getPendingDebtAtArchiving(),
                entity.getVersion());
    }

    private boolean applyManagementRequest(DeedManagement entity, ManagementRequest request) {
        if (request.number() != null) {
            entity.setNumber(request.number());
        }
        entity.setHeading(request.heading());
        entity.setDateStart(request.dateStart() != null ? request.dateStart() : new Date());
        String notes = request.notes();
        if (request.pendingDebtAtArchiving() != null) {
            entity.setPendingDebtAtArchiving(request.pendingDebtAtArchiving());
        }
        if (request.notaryPersonId() != null) {
            Person requestedNotary = personRepository.findById(request.notaryPersonId()).orElse(null);
            if (requestedNotary == null) {
                return false;
            }
            ManagementSubstitutionService.AssignedNotary assigned =
                    managementSubstitutionService.resolveNotary(requestedNotary, entity.getDateStart());
            entity.setFkIdNotaryPerson(assigned.notary());
            notes = buildNotes(notes, requestedNotary, assigned);
        }
        entity.setNotes(notes);
        if (request.managementStatusId() != null) {
            ManagementStatus status = statusRepository.findById(request.managementStatusId())
                    .orElse(null);
            if (status == null) {
                return false;
            }
            entity.setFkIdManagementStatus(status);
        }
        return true;
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Bad request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Transactional
    @Operation(summary = "CU02 - Create a new management",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU13 - When a status is provided, the initial status is appended to History. "
                    + "Plain create has no procedure/workflow yet, so any defined status is accepted.")
    public ResponseEntity<Object> create(@Valid @RequestBody ManagementRequest request) {
        try {
            DeedManagement entity = new DeedManagement();
            if (!applyManagementRequest(entity, request)) {
                return ResponseEntity.badRequest().build();
            }
            entity = repository.save(entity);
            if (entity.getFkIdManagementStatus() != null) {
                statusLogService.registerStatus(entity, null);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(toManagementResponse(entity));
        } catch (NotaireException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create management", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Status mutation rejected — use POST /{id}/transition"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "CU53 - Update a management",
            description = "CU22/CU59 - If the requested notary has an active substitution for the management "
                    + "start date, the management is redirected to the substitute and a note is recorded. "
                    + "CU83 **BREAKING** - Changing managementStatusId is rejected; use "
                    + "POST /{id}/transition. Same-status updates remain allowed. "
                    + "CU13 - History for status changes is written by /transition (and create).")
    public ResponseEntity<Void> update(@PathVariable Integer id, @Valid @RequestBody ManagementRequest request) {
        return repository.findById(id).map(existing -> {
            try {
                Integer previousStatusId = statusIdOf(existing);
                managementStatusWriteGuard.rejectStatusMutationOnUpdate(
                        previousStatusId, request.managementStatusId());
                if (!applyManagementRequest(existing, request)) {
                    return ResponseEntity.badRequest().<Void>build();
                }
                repository.save(existing);
                registerStatusChangeIfNeeded(existing, previousStatusId);
                return ResponseEntity.ok().<Void>build();
            } catch (NotaireException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to update management id {}", id, e);
                return ResponseEntity.internalServerError().<Void>build();
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private static Integer statusIdOf(DeedManagement management) {
        ManagementStatus status = management.getFkIdManagementStatus();
        return status != null ? status.getIdManagementStatus() : null;
    }

    /**
     * Appends a History row when status is first assigned or changes. Does not invent a row
     * when status remains null or unchanged. Reuses {@link ManagementStatusLogService}.
     */
    private void registerStatusChangeIfNeeded(DeedManagement management, Integer previousStatusId) {
        Integer newStatusId = statusIdOf(management);
        if (newStatusId != null && !newStatusId.equals(previousStatusId)) {
            statusLogService.registerStatus(management, null);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete management")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete management id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
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
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getWorkflowTrace(@PathVariable Integer id) {
        try {
            DtoManagementWorkflowTrace trace = workflowTraceService.buildTrace(id);
            return ResponseEntity.ok(trace);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot build workflow trace for management {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
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
            java.math.BigDecimal pendingBalance = managementArchiveDebtService.calculatePendingBalance(id);
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
            return ResponseEntity.ok(financialSummaryService.getSummary(id));
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
        try {
            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(id, confirmed);
            return ResponseEntity.ok(new DtoArchivedManagement(result.management().getIdManagement(),
                    result.pendingBalance(), Boolean.TRUE.equals(result.management().getPendingDebtAtArchiving())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (FoldersInWaitException e) {
            List<Integer> waitingFolderNumbers = e.getFoldersInWait().stream()
                    .map(ProcedureFolder::getNumber)
                    .toList();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "waitingFolderNumbers", waitingFolderNumbers));
        }
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
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorySummary>> getHistory(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<DtoHistorySummary> history = statusLogService.getHistory(id).stream()
                .map(com.licensis.notaire.application.usecase.history.HistoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(history);
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
        return ResponseEntity.ok(documentReentryService.getRequiredDocumentation(id));
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
        DtoDocumentReentered result = documentReentryService.reenter(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
