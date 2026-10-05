package com.licensis.notaire.adapter.in.web.management;

import com.licensis.notaire.dto.DtoDocumentEntidadExterna;
import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementDocumentsEntidadesExternas;
import com.licensis.notaire.dto.DtoManagementReingresoDocumentacion;
import com.licensis.notaire.dto.DtoManagementResumenFinanciero;
import com.licensis.notaire.dto.DtoManagementSummary;
import com.licensis.notaire.dto.DtoManagementWorkflowTrace;
import com.licensis.notaire.dto.DtoHistorySummary;
import com.licensis.notaire.dto.DtoMovementDocumentEntidadExterna;
import com.licensis.notaire.dto.DtoReingresoDocumentacionRequest;
import com.licensis.notaire.exception.CarpetasEnWaitException;
import com.licensis.notaire.business.ProcedureFolder;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
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
import com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService;
import com.licensis.notaire.application.usecase.management.ManagementArchiveDebtService;
import com.licensis.notaire.application.usecase.management.ManagementBitacoraService;
import com.licensis.notaire.application.usecase.management.ManagementQueryService;
import com.licensis.notaire.application.usecase.management.ManagementResumenFinancieroService;
import com.licensis.notaire.application.usecase.management.ManagementStatusWriteGuard;
import com.licensis.notaire.application.usecase.management.ManagementSubstitutionService;
import com.licensis.notaire.exception.NotaireException;
import com.licensis.notaire.application.usecase.workflow.ReingresoDocumentacionService;
import com.licensis.notaire.application.usecase.workflow.WorkflowTraceService;
import com.licensis.notaire.application.port.in.management.TransitionManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/gestiones")
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
    private final ManagementResumenFinancieroService managementResumenFinancieroService;
    private final ManagementBitacoraService managementBitacoraService;
    private final ManagementStatusWriteGuard managementStatusWriteGuard;
    private final TransitionManagementUseCase transitionManagementUseCase;
    private final TransitionManagementWebMapper transitionManagementWebMapper;
    private final ExternalEntityDocumentService documentEntidadExternaService;
    private final ReingresoDocumentacionService reingresoDocumentacionService;
    private final ProcedureFolderService procedureFolderService;

    public ManagementController(DeedManagementRepository repository,
                             HistoryRepository historyRepository,
                             WorkflowTraceService workflowTraceService,
                             ManagementQueryService managementQueryService, PersonRepository personRepository,
                             ManagementStatusRepository statusRepository, BudgetRepository budgetRepository,
                             ProcedureTypeRepository typeProcedureRepository, ProcedureRepository procedureRepository,
                             PropertyRepository propertyRepository,
                             ManagementArchiveDebtService managementArchiveDebtService,
                             ManagementSubstitutionService managementSubstitutionService,
                             ManagementResumenFinancieroService managementResumenFinancieroService,
                             ManagementBitacoraService managementBitacoraService,
                             ManagementStatusWriteGuard managementStatusWriteGuard,
                             TransitionManagementUseCase transitionManagementUseCase,
                             TransitionManagementWebMapper transitionManagementWebMapper,
                             ExternalEntityDocumentService documentEntidadExternaService,
                             ReingresoDocumentacionService reingresoDocumentacionService,
                             ProcedureFolderService procedureFolderService) {
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
        this.managementResumenFinancieroService = managementResumenFinancieroService;
        this.managementBitacoraService = managementBitacoraService;
        this.managementStatusWriteGuard = managementStatusWriteGuard;
        this.transitionManagementUseCase = transitionManagementUseCase;
        this.transitionManagementWebMapper = transitionManagementWebMapper;
        this.documentEntidadExternaService = documentEntidadExternaService;
        this.reingresoDocumentacionService = reingresoDocumentacionService;
        this.procedureFolderService = procedureFolderService;
    }

    public record DtoSaldoPending(java.math.BigDecimal pendingBalance) {}

    public record DtoArchivedManagement(Integer idManagement, java.math.BigDecimal pendingBalance, boolean pendingDebtAtArchiving) {}

    public record DtoTransitionRequest(String statusDestination) {}

    public record CompleteCaseRequest(Integer number, String encabezado, String notes,
            Integer budgetId, Integer notaryId, Integer statusManagementId, Integer typeProcedureId,
            Integer propertyId) {}

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
        management.setEncabezado(request.encabezado() == null ? "Management" : request.encabezado());
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
        managementBitacoraService.registerStatus(management, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managementQueryService.findById(management.getIdManagement()).orElseThrow());
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
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(managementQueryService.findAll(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get management by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getById(@PathVariable Integer id) {
        return managementQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{number}")
    @Operation(summary = "Get management by number")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getByNumber(@PathVariable Integer number) {
        return managementQueryService.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idPerson}")
    @Operation(summary = "CU19 - List managements for a client")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ManagementResponse>> getByClient(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(repository.findByClientPersonId(idPerson).stream()
                .map(this::toManagementResponse).toList());
    }

    @GetMapping("/{id}/estado-actual")
    @Operation(summary = "CU13/CU14 - Get current management status",
            description = "Returns the latest History row by date when present; otherwise synthesizes a "
                    + "summary from DeedManagement.fkIdManagementStatus (as-of-now). Missing management "
                    + "or null status yields 404.")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorySummary> getStatusActual(@PathVariable Integer id) {
        Optional<DeedManagement> managementOpt = repository.findById(id);
        if (managementOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<History> historyRows = historyRepository.findByFkIdManagementIdManagement(id);
        if (!historyRows.isEmpty()) {
            return historyRows.stream()
                    .max(Comparator.comparing(History::getDate)
                            .thenComparing(History::getIdHistory))
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
            @NotBlank String encabezado,
            Date dateStart,
            String notes,
            Integer notaryPersonId,
            Integer managementStatusId,
            Boolean pendingDebtAtArchiving) {}

    record ManagementResponse(
            Integer idManagement,
            int number,
            String encabezado,
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
                entity.getEncabezado(),
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
        entity.setEncabezado(request.encabezado());
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
                managementBitacoraService.registerStatus(entity, null);
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
     * when status remains null or unchanged. Reuses {@link ManagementBitacoraService}.
     */
    private void registerStatusChangeIfNeeded(DeedManagement management, Integer previousStatusId) {
        Integer newStatusId = statusIdOf(management);
        if (newStatusId != null && !newStatusId.equals(previousStatusId)) {
            managementBitacoraService.registerStatus(management, null);
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
            return ResponseEntity.ok().build();
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
                    + "status equals `statusActual`). The managements UI must offer only those "
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
    @GetMapping("/{id}/saldo-pendiente")
    @Operation(summary = "CU16 - Calculate aggregated pending balance for a management (RF-22)")
    public ResponseEntity<DtoSaldoPending> getSaldoPending(@PathVariable Integer id) {
        try {
            java.math.BigDecimal saldo = managementArchiveDebtService.calculatePendingBalance(id);
            return ResponseEntity.ok(new DtoSaldoPending(saldo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/resumen-financiero")
    @Operation(summary = "CU47/CU02 - Get aggregated financial summary for a management")
    public ResponseEntity<DtoManagementResumenFinanciero> getResumenFinanciero(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(managementResumenFinancieroService.getSummary(id));
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
    @PostMapping("/{id}/archivar")
    @Operation(summary = "CU16 - Archive a management, warning and recording pending debt (RF-22, RF-37); "
            + "cascades to CU85 procedure folders")
    public ResponseEntity<Object> archiving(@PathVariable Integer id,
            @RequestParam(name = "confirmado", defaultValue = "false") boolean confirmado) {
        try {
            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(id, confirmado);
            return ResponseEntity.ok(new DtoArchivedManagement(result.management().getIdManagement(),
                    result.pendingBalance(), Boolean.TRUE.equals(result.management().getPendingDebtAtArchiving())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (CarpetasEnWaitException e) {
            List<Integer> waitingFolderNumbers = e.getCarpetasEnWait().stream()
                    .map(ProcedureFolder::getNumber)
                    .toList();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "carpetasEnEsperaNumero", waitingFolderNumbers));
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
    @GetMapping("/{id}/historial")
    @Operation(summary = "CU13 - Get the full management History log, ordered chronologically")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorySummary>> getHistory(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<DtoHistorySummary> history = managementBitacoraService.getHistory(id).stream()
                .map(com.licensis.notaire.application.usecase.history.HistoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(history);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/documentos-entidades-externas")
    @Operation(summary = "CU10 - Get management documentation handled by external entities")
    public ResponseEntity<DtoManagementDocumentsEntidadesExternas> getDocumentsEntidadesExternas(
            @PathVariable Integer id) {
        return ResponseEntity.ok(documentEntidadExternaService.getDocuments(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movement registered"),
        @ApiResponse(responseCode = "400", description = "Invalid document for the management"),
        @ApiResponse(responseCode = "404", description = "Management or document not found")
    })
    @PutMapping("/{id}/documentos-entidades-externas/{idSubmittedDocument}")
    @Operation(summary = "CU10 - Register movement of an external-entity document")
    public ResponseEntity<DtoDocumentEntidadExterna> registerMovementDocumentEntidadExterna(
            @PathVariable Integer id, @PathVariable Integer idSubmittedDocument,
            @RequestBody DtoMovementDocumentEntidadExterna movement) {
        DtoDocumentEntidadExterna result =
                documentEntidadExternaService.registerMovement(id, idSubmittedDocument, movement);
        documentEntidadExternaService.tryCompleteDocumentation(id);
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Management not found")
    })
    @GetMapping("/{id}/reingreso-documentacion")
    @Operation(summary = "CU43 - Get management procedures with required documentation")
    public ResponseEntity<DtoManagementReingresoDocumentacion> getRequiredDocumentationForReentry(
            @PathVariable Integer id) {
        return ResponseEntity.ok(reingresoDocumentacionService.getRequiredDocumentation(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Document re-entered"),
        @ApiResponse(responseCode = "400", description = "Invalid procedure or document type"),
        @ApiResponse(responseCode = "404", description = "Management or procedure not found")
    })
    @PostMapping("/{id}/reingreso-documentacion")
    @Operation(summary = "CU43 - Re-enter a document type for a management procedure")
    public ResponseEntity<DtoDocumentReentered> reenterDocumentation(@PathVariable Integer id,
            @RequestBody DtoReingresoDocumentacionRequest request) {
        DtoDocumentReentered result = reingresoDocumentacionService.reenter(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
