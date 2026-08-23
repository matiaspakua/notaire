package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoGestionResumenFinanciero;
import com.licensis.notaire.dto.DtoGestionSummary;
import com.licensis.notaire.dto.DtoGestionWorkflowTrace;
import com.licensis.notaire.dto.DtoHistorialSummary;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.InmuebleRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.GestionArchiveDebtService;
import com.licensis.notaire.service.GestionQueryService;
import com.licensis.notaire.service.GestionResumenFinancieroService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Gestiones", description = "API para gestionar gestiones de escritura")
public class GestionController {

    private static final Logger log = LoggerFactory.getLogger(GestionController.class);

    private final GestionDeEscrituraRepository repository;
    private final HistorialRepository historialRepository;
    private final WorkflowTraceService workflowTraceService;
    private final GestionQueryService gestionQueryService;
    private final PersonaRepository personaRepository;
    private final EstadoDeGestionRepository estadoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final TipoDeTramiteRepository tipoTramiteRepository;
    private final TramiteRepository tramiteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final GestionArchiveDebtService gestionArchiveDebtService;
    private final GestionResumenFinancieroService gestionResumenFinancieroService;

    public GestionController(GestionDeEscrituraRepository repository,
                             HistorialRepository historialRepository,
                             WorkflowTraceService workflowTraceService,
                             GestionQueryService gestionQueryService, PersonaRepository personaRepository,
                             EstadoDeGestionRepository estadoRepository, PresupuestoRepository presupuestoRepository,
                             TipoDeTramiteRepository tipoTramiteRepository, TramiteRepository tramiteRepository,
                             InmuebleRepository inmuebleRepository,
                             GestionArchiveDebtService gestionArchiveDebtService,
                             GestionResumenFinancieroService gestionResumenFinancieroService) {
        this.repository = repository;
        this.historialRepository = historialRepository;
        this.workflowTraceService = workflowTraceService;
        this.gestionQueryService = gestionQueryService;
        this.personaRepository = personaRepository;
        this.estadoRepository = estadoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.tipoTramiteRepository = tipoTramiteRepository;
        this.tramiteRepository = tramiteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.gestionArchiveDebtService = gestionArchiveDebtService;
        this.gestionResumenFinancieroService = gestionResumenFinancieroService;
    }

    public record DtoSaldoPendiente(Float saldoPendiente) {}

    public record DtoGestionArchivada(Integer idGestion, Float saldoPendiente, boolean deudaPendienteAlArchivar) {}

    public record CompleteCaseRequest(Integer numero, String encabezado, String observaciones,
            Integer presupuestoId, Integer escribanoId, Integer estadoGestionId, Integer tipoTramiteId,
            Integer inmuebleId) {}

    private record CaseDependencies(Presupuesto presupuesto, Persona escribano, EstadoDeGestion estado,
            TipoDeTramite tipoTramite, Inmueble inmueble) {}

    private static boolean hasRequiredFields(CompleteCaseRequest request) {
        return request.numero() != null && request.presupuestoId() != null && request.escribanoId() != null
                && request.estadoGestionId() != null && request.tipoTramiteId() != null;
    }

    private Optional<CaseDependencies> resolveDependencies(CompleteCaseRequest request) {
        Optional<Presupuesto> presupuesto = presupuestoRepository.findById(request.presupuestoId());
        Optional<Persona> escribano = personaRepository.findById(request.escribanoId());
        Optional<EstadoDeGestion> estado = estadoRepository.findById(request.estadoGestionId());
        Optional<TipoDeTramite> tipoTramite = tipoTramiteRepository.findById(request.tipoTramiteId());
        if (presupuesto.isEmpty() || escribano.isEmpty() || estado.isEmpty() || tipoTramite.isEmpty()) {
            return Optional.empty();
        }
        Inmueble inmueble = null;
        if (request.inmuebleId() != null) {
            Optional<Inmueble> found = inmuebleRepository.findById(request.inmuebleId());
            if (found.isEmpty()) {
                return Optional.empty();
            }
            inmueble = found.get();
        }
        return Optional.of(new CaseDependencies(presupuesto.get(), escribano.get(), estado.get(),
                tipoTramite.get(), inmueble));
    }

    private static void applyGestionFields(GestionDeEscritura gestion, CompleteCaseRequest request,
            CaseDependencies dependencies) {
        gestion.setNumero(request.numero());
        gestion.setEncabezado(request.encabezado() == null ? "Gestión" : request.encabezado());
        gestion.setObservaciones(request.observaciones());
        gestion.setFkIdPersonaEscribano(dependencies.escribano());
        gestion.setFkIdEstadoDeGestion(dependencies.estado());
    }

    private void saveTramite(GestionDeEscritura gestion, CaseDependencies dependencies) {
        Tramite tramite = new Tramite();
        tramite.setFkIdGestion(gestion);
        applyTramiteDependencies(tramite, dependencies);
        tramiteRepository.save(tramite);
    }

    private void updateTramite(Tramite tramite, CaseDependencies dependencies) {
        applyTramiteDependencies(tramite, dependencies);
        tramiteRepository.save(tramite);
    }

    private static void applyTramiteDependencies(Tramite tramite, CaseDependencies dependencies) {
        tramite.setFkIdPresupuesto(dependencies.presupuesto());
        tramite.setFkIdTipoTramite(dependencies.tipoTramite());
        if (dependencies.inmueble() != null) {
            tramite.setFkIdInmueble(dependencies.inmueble());
        }
    }

    @PostMapping("/complete-case")
    @Transactional
    @Operation(summary = "CU02 - Crear una gestión con sus dependencias obligatorias")
    public ResponseEntity<Object> createCompleteCase(@RequestBody CompleteCaseRequest request) {
        if (!hasRequiredFields(request)) {
            return ResponseEntity.badRequest().build();
        }
        Optional<CaseDependencies> dependencies = resolveDependencies(request);
        if (dependencies.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setFechaInicio(new Date());
        applyGestionFields(gestion, request, dependencies.get());
        gestion = repository.save(gestion);
        saveTramite(gestion, dependencies.get());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gestionQueryService.findById(gestion.getIdGestion()).orElseThrow());
    }

    @PutMapping("/{id}/complete-case")
    @Transactional
    @Operation(summary = "CU02 - Actualizar una gestión junto con sus dependencias obligatorias")
    public ResponseEntity<Object> updateCompleteCase(@PathVariable Integer id,
            @RequestBody CompleteCaseRequest request) {
        Optional<GestionDeEscritura> existing = repository.findById(id);
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
        GestionDeEscritura gestion = existing.get();
        applyGestionFields(gestion, request, dependencies.get());
        gestion = repository.save(gestion);
        List<Tramite> tramites = tramiteRepository.findByFkIdGestionIdGestion(id);
        if (tramites.isEmpty()) {
            saveTramite(gestion, dependencies.get());
        } else {
            updateTramite(tramites.get(0), dependencies.get());
        }
        return ResponseEntity.ok(gestionQueryService.findById(gestion.getIdGestion()).orElseThrow());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las gestiones")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DtoGestionSummary>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(gestionQueryService.findAll(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener gestion por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoGestionSummary> getById(@PathVariable Integer id) {
        return gestionQueryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener gestion por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoGestionSummary> getByNumero(@PathVariable Integer numero) {
        return gestionQueryService.findByNumero(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{idPersona}")
    @Operation(summary = "Obtener gestiones de un cliente (CU19)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GestionDeEscritura>> getByCliente(@PathVariable Integer idPersona) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}/estado-actual")
    @Operation(summary = "Obtener estado actual de una gestion")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoHistorialSummary> getEstadoActual(@PathVariable Integer id) {
        List<Historial> historiales = historialRepository.findByFkIdGestionIdGestion(id);
        if (historiales.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return historiales.stream()
            .max(Comparator.comparing(Historial::getFecha))
            .map(h -> ResponseEntity.ok(com.licensis.notaire.service.mappers.HistorialMapper.toDto(h)))
            .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva gestion")
    public ResponseEntity<Object> create(@RequestBody GestionDeEscritura entity) {
        entity = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gestion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody GestionDeEscritura entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdGestion(id);
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
            DtoGestionWorkflowTrace trace = workflowTraceService.buildTrace(id);
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
    public ResponseEntity<DtoSaldoPendiente> getSaldoPendiente(@PathVariable Integer id) {
        try {
            Float saldo = gestionArchiveDebtService.calcularSaldoPendiente(id);
            return ResponseEntity.ok(new DtoSaldoPendiente(saldo));
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
    public ResponseEntity<DtoGestionResumenFinanciero> getResumenFinanciero(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(gestionResumenFinancieroService.obtenerResumen(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Gestión archivada"),
        @ApiResponse(responseCode = "404", description = "Gestion no encontrada")
    })
    @PostMapping("/{id}/archivar")
    @Operation(summary = "CU16 - Archivar una gestión advirtiendo y registrando deuda pendiente (RF-22, RF-37)")
    public ResponseEntity<DtoGestionArchivada> archivar(@PathVariable Integer id) {
        try {
            GestionArchiveDebtService.ArchiveResult result = gestionArchiveDebtService.archivar(id);
            return ResponseEntity.ok(new DtoGestionArchivada(result.gestion().getIdGestion(),
                    result.saldoPendiente(), Boolean.TRUE.equals(result.gestion().getDeudaPendienteAlArchivar())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
