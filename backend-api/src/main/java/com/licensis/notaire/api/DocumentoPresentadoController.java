package com.licensis.notaire.api;

import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TramiteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/documento-presentado")
@Tag(name = "DocumentoPresentado", description = "API para gestionar documentos presentados")
public class DocumentoPresentadoController {

    private static final Logger log = LoggerFactory.getLogger(DocumentoPresentadoController.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    record TipoDocInfo(Integer idTipoDocumento, String nombre) {}

    record DocumentoPresentadoResponse(
            Integer idDocumentoPresentado,
            TipoDocInfo tipo,
            String fecha,
            Boolean entregado
    ) {}

    record DocumentoPresentadoRequest(Integer tipoId, String fecha, Boolean entregado, Integer tramiteId,
            String quienEntrega, String nombre) {}

    private final DocumentoPresentadoRepository repository;
    private final TipoDeDocumentoRepository tipoRepository;
    private final TramiteRepository tramiteRepository;

    public DocumentoPresentadoController(DocumentoPresentadoRepository repository,
                                         TipoDeDocumentoRepository tipoRepository,
                                         TramiteRepository tramiteRepository) {
        this.repository = repository;
        this.tipoRepository = tipoRepository;
        this.tramiteRepository = tramiteRepository;
    }

    private DocumentoPresentado toEntity(DocumentoPresentadoRequest request) {
        DocumentoPresentado entity = new DocumentoPresentado();
        entity.setFkIdTipoDocumento(request.tipoId());
        entity.setEntregado(request.entregado() != null ? request.entregado() : false);
        entity.setNombre(request.nombre() != null ? request.nombre() : "");
        entity.setPreparado(false);
        entity.setLiberado(false);
        entity.setObservado(false);
        entity.setReingresado(false);
        if (request.tramiteId() != null) {
            Tramite tramite = tramiteRepository.findById(request.tramiteId()).orElse(null);
            entity.setFkIdTramite(tramite);
        }
        if (request.fecha() != null) {
            try {
                entity.setFechaIngreso(DATE_FORMAT.parse(request.fecha()));
            } catch (ParseException e) {
                log.warn("Invalid fecha format: {}", request.fecha());
            }
        }
        applyVencimientoFromTipoDeDocumento(entity, request);
        return entity;
    }

    private void applyVencimientoFromTipoDeDocumento(DocumentoPresentado entity, DocumentoPresentadoRequest request) {
        Optional<TipoDeDocumento> tipo = request.tipoId() != null
                ? tipoRepository.findById(request.tipoId())
                : Optional.empty();

        boolean vence = tipo.map(TipoDeDocumento::getVence).orElse(false);
        Integer diasVencimiento = tipo.map(TipoDeDocumento::getDiasVencimiento).orElse(null);
        String quienEntrega = request.quienEntrega() != null
                ? request.quienEntrega()
                : tipo.map(TipoDeDocumento::getQuienEntrega).orElse("");

        entity.setVence(vence);
        entity.setDiasVencimiento(diasVencimiento);
        entity.setQuienEntrega(quienEntrega);

        if (vence && diasVencimiento != null && entity.getFechaIngreso() != null) {
            Date fechaVencimiento = Date.from(
                    entity.getFechaIngreso().toInstant().plus(diasVencimiento, ChronoUnit.DAYS));
            entity.setFechaVencimiento(fechaVencimiento);
        }
    }

    private DocumentoPresentadoResponse toResponse(DocumentoPresentado d) {
        TipoDocInfo tipo = null;
        Integer tipoId = d.getFkIdTipoDocumentoNullable();
        if (tipoId != null) {
            tipo = tipoRepository.findById(tipoId)
                    .map(t -> new TipoDocInfo(t.getIdTipoDocumento(), t.getNombre()))
                    .orElse(null);
        }
        String fecha = d.getFechaIngreso() != null ? DATE_FORMAT.format(d.getFechaIngreso()) : null;
        return new DocumentoPresentadoResponse(d.getIdDocumentoPresentado(), tipo, fecha, d.getEntregado());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los documentos presentados")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DocumentoPresentadoResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento presentado por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DocumentoPresentadoResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo documento presentado")
    public ResponseEntity<Object> create(@RequestBody DocumentoPresentadoRequest request) {
        try {
            DocumentoPresentado entity = toEntity(request);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create documento presentado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento presentado")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody DocumentoPresentadoRequest request) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            DocumentoPresentado entity = toEntity(request);
            entity.setIdDocumentoPresentado(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update documento presentado id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento presentado")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete documento presentado id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
