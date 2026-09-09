package com.licensis.notaire.api;

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
import com.licensis.notaire.service.ProcedureFolderService;
import com.licensis.notaire.service.DocumentEntidadExternaService;
import com.licensis.notaire.service.ManagementArchiveDebtService;
import com.licensis.notaire.service.ManagementBitacoraService;
import com.licensis.notaire.service.ManagementQueryService;
import com.licensis.notaire.service.ManagementResumenFinancieroService;
import com.licensis.notaire.service.ManagementSubstitutionService;
import com.licensis.notaire.service.ManagementTransitionService;
import com.licensis.notaire.service.ReingresoDocumentacionService;
import com.licensis.notaire.service.WorkflowTraceService;
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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Gestiones", description = "API para gestionar gestiones de escritura")
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
    private final ManagementTransitionService managementTransitionService;
    private final DocumentEntidadExternaService documentEntidadExternaService;
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
                             ManagementTransitionService managementTransitionService,
                             DocumentEntidadExternaService documentEntidadExternaService,
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
        this.managementTransitionService = managementTransitionService;
        this.documentEntidadExternaService = documentEntidadExternaService;
        this.reingresoDocumentacionService = reingresoDocumentacionService;
        this.procedureFolderService = procedureFolderService;
    }

    public record DtoSaldoPending(Float saldoPending) {}

    public record DtoManagementArchivada(Integer idManagement, Float saldoPending, boolean pendingDebtAtArchiving) {}

    public record DtoTransicionRequest(String statusDestination) {}

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
        management.setEncabezado(request.encabezado() == null ? "Gestión" : request.encabezado());
        ManagementSubstitutionService.NotaryAsignado asignado =
                managementSubstitutionService.resolverNotary(dependencies.notary(), management.getDateStart());
        management.setFkIdNotaryPerson(asignado.notary());
        management.setNotes(buildNotes(request.notes(), dependencies.notary(), asignado));
        management.setFkIdManagementStatus(dependencies.status());
    }

    private String buildNotes(String requestNotes, Person notarySolicitado,
            ManagementSubstitutionService.NotaryAsignado asignado) {
        if (asignado.substitutionAplicada() == null) {
            return requestNotes;
        }
        String redireccion = managementSubstitutionService.observacionRedireccion(notarySolicitado, asignado.notary());
        if (requestNotes == null || requestNotes.isBlank()) {
            return redireccion;
        }
        return requestNotes + " | " + redireccion;
    }

    private void saveProcedure(DeedManagement management, CaseDependencies dependencies) {
        Procedure procedure = new Procedure();
        procedure.setFkIdManagement(management);
        applyProcedureDependencies(procedure, dependencies);
        Procedure guardado = procedureRepository.save(procedure);
        procedureFolderService.generarFolderParaProcedure(guardado);
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
    @Operation(summary = "CU02 - Crear una gestión con sus dependencias obligatorias",
            description = "CU22/CU59 - Si el escribano solicitado tiene una suplencia activa para la fecha de "
                    + "inicio de la gestión, la gestión se redirige automáticamente al suplente y se deja "
                    + "constancia en el campo observaciones de la respuesta.")
    public ResponseEntity<Object> createCompleteCase(@RequestBody CompleteCaseRequest request) {
        if (!hasRequiredFields(request)) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        applyManagementFields(management, request, dependencies.get());
        management = repository.save(management);
        saveProcedure(management, dependencies.get());
        managementBitacoraService.registrarStatus(management, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @PutMapping("/{id}/complete-case")
    @Transactional
    @Operation(summary = "CU02 - Actualizar una gestión junto con sus dependencias obligatorias",
            description = "CU22/CU59 - Si el escribano solicitado tiene una suplencia activa para la fecha de "
                    + "inicio de la gestión, la gestión se redirige automáticamente al suplente y se deja "
                    + "constancia en el campo observaciones de la respuesta.")
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
        applyManagementFields(management, request, dependencies.get());
        management = repository.save(management);
        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(id);
        if (procedures.isEmpty()) {
            saveProcedure(management, dependencies.get());
        } else {
            updateProcedure(procedures.get(0), dependencies.get());
        }
        return ResponseEntity.ok(managementQueryService.findById(management.getIdManagement()).orElseThrow());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las gestiones")
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
    @Operation(summary = "Obtener gestion por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getById(@PathVariable Integer id) {
        return managementQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{number}")
    @Operation(summary = "Obtener gestion por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoManagementSummary> getByNumber(@PathVariable Integer number) {
        return managementQueryService.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idPerson}")
    @Operation(summary = "Obtener gestiones de un cliente (CU19)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DeedManagement>> getByClient(@PathVariable Integer idPerson) {
        return ResponseEntity.ok(repository.findByClientPersonId(idPerson));
    }

    @GetMapping("/{id}/estado-actual")
    @Operation(summary = "Obtener estado actual de una gestion")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorySummary> getStatusActual(@PathVariable Integer id) {
        List<History> historiales = historyRepository.findByFkIdManagementIdManagement(id);
        if (historiales.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return historiales.stream()
            .max(Comparator.comparing(History::getDate))
            .map(h -> ResponseEntity.ok(com.licensis.notaire.service.mappers.HistoryMapper.toDto(h)))
            .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva gestion")
    public ResponseEntity<Object> create(@RequestBody DeedManagement entity) {
        entity = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gestion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody DeedManagement entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdManagement(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update gestion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar gestion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete gestion id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada"),
        @ApiResponse(responseCode = "400", description = "Gestion sin tramites o workflow definition")
    })
    @GetMapping("/{id}/workflow-trace")
    @Operation(summary = "Obtener trace del workflow de una gestion (con nodos, transiciones, historial y estados)")
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getWorkflowTrace(@PathVariable Integer id) {
        try {
            DtoManagementWorkflowTrace trace = workflowTraceService.buildTrace(id);
            return ResponseEntity.ok(trace);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot build workflow trace for gestion {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to build workflow trace for gestion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @GetMapping("/{id}/saldo-pendiente")
    @Operation(summary = "CU16 - Calcular saldo pendiente agregado de una gestión (RF-22)")
    public ResponseEntity<DtoSaldoPending> getSaldoPending(@PathVariable Integer id) {
        try {
            Float saldo = managementArchiveDebtService.calcularSaldoPending(id);
            return ResponseEntity.ok(new DtoSaldoPending(saldo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @GetMapping("/{id}/resumen-financiero")
    @Operation(summary = "CU47/CU02 - Obtener resumen financiero agregado de una gestión")
    public ResponseEntity<DtoManagementResumenFinanciero> getResumenFinanciero(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(managementResumenFinancieroService.obtenerResumen(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gestión archivada"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada"),
        @ApiResponse(responseCode = "409",
                description = "Hay carpetas de trámite (CU85) en espera sin resolver; confirme para archivar")
    })
    @PostMapping("/{id}/archivar")
    @Operation(summary = "CU16 - Archivar una gestión advirtiendo y registrando deuda pendiente (RF-22, RF-37); "
            + "cascada a las carpetas de trámite de CU85")
    public ResponseEntity<Object> archiving(@PathVariable Integer id,
            @RequestParam(name = "confirmado", defaultValue = "false") boolean confirmado) {
        try {
            ManagementArchiveDebtService.ArchiveResult result = managementArchiveDebtService.archiving(id, confirmado);
            return ResponseEntity.ok(new DtoManagementArchivada(result.management().getIdManagement(),
                    result.saldoPending(), Boolean.TRUE.equals(result.management().getPendingDebtAtArchiving())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (CarpetasEnWaitException e) {
            List<Integer> numerosCarpetasEnWait = e.getCarpetasEnWait().stream()
                    .map(ProcedureFolder::getNumber)
                    .toList();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage(), "carpetasEnEsperaNumero", numerosCarpetasEnWait));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transición aplicada"),
        @ApiResponse(responseCode = "400", description = "Transición no válida para el workflow del tipo de trámite"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @PostMapping("/{id}/transicionar")
    @Operation(summary = "CU83 - Transicionar el estado de una gestión validando el workflow definido")
    public ResponseEntity<DtoManagementSummary> transicionar(@PathVariable Integer id,
            @RequestBody DtoTransicionRequest request) {
        managementTransitionService.transicionar(id, request.statusDestination());
        return ResponseEntity.ok(managementQueryService.findById(id).orElseThrow());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @GetMapping("/{id}/historial")
    @Operation(summary = "CU13 - Obtener la bitácora completa de una gestión, ordenada cronológicamente")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoHistorySummary>> getHistory(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<DtoHistorySummary> history = managementBitacoraService.obtenerHistory(id).stream()
                .map(com.licensis.notaire.service.mappers.HistoryMapper::toDto)
                .toList();
        return ResponseEntity.ok(history);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @GetMapping("/{id}/documentos-entidades-externas")
    @Operation(summary = "CU10 - Obtener la documentación de una gestión a cargo de entidades externas")
    public ResponseEntity<DtoManagementDocumentsEntidadesExternas> getDocumentsEntidadesExternas(
            @PathVariable Integer id) {
        return ResponseEntity.ok(documentEntidadExternaService.obtenerDocuments(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento registrado"),
        @ApiResponse(responseCode = "400", description = "Documento inválido para la gestión"),
        @ApiResponse(responseCode = "404", description = "Gestion o documento no encontrado")
    })
    @PutMapping("/{id}/documentos-entidades-externas/{idSubmittedDocument}")
    @Operation(summary = "CU10 - Registrar el movimiento de un documento de entidad externa")
    public ResponseEntity<DtoDocumentEntidadExterna> registrarMovementDocumentEntidadExterna(
            @PathVariable Integer id, @PathVariable Integer idSubmittedDocument,
            @RequestBody DtoMovementDocumentEntidadExterna movement) {
        DtoDocumentEntidadExterna resultado =
                documentEntidadExternaService.registrarMovement(id, idSubmittedDocument, movement);
        documentEntidadExternaService.intentarCompletarDocumentacion(id);
        return ResponseEntity.ok(resultado);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @GetMapping("/{id}/reingreso-documentacion")
    @Operation(summary = "CU43 - Obtener los trámites de una gestión con su documentación necesaria")
    public ResponseEntity<DtoManagementReingresoDocumentacion> getDocumentacionNecesariaReingreso(
            @PathVariable Integer id) {
        return ResponseEntity.ok(reingresoDocumentacionService.obtenerDocumentacionNecesaria(id));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Documento reingresado"),
        @ApiResponse(responseCode = "400", description = "Trámite o tipo de documento inválido"),
        @ApiResponse(responseCode = "404", description = "Gestion o trámite no encontrado")
    })
    @PostMapping("/{id}/reingreso-documentacion")
    @Operation(summary = "CU43 - Reingresar un tipo de documento para un trámite de la gestión")
    public ResponseEntity<DtoDocumentReentered> reingresarDocumentacion(@PathVariable Integer id,
            @RequestBody DtoReingresoDocumentacionRequest request) {
        DtoDocumentReentered resultado = reingresoDocumentacionService.reingresar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
