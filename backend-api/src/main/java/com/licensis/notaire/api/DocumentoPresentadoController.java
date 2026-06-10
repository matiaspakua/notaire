package com.licensis.notaire.api;

import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

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

    record DocumentoPresentadoRequest(Integer tipoId, String fecha, Boolean entregado) {}

    private final DocumentoPresentadoRepository repository;
    private final TipoDeDocumentoRepository tipoRepository;

    public DocumentoPresentadoController(DocumentoPresentadoRepository repository,
                                         TipoDeDocumentoRepository tipoRepository) {
        this.repository = repository;
        this.tipoRepository = tipoRepository;
    }

    private DocumentoPresentado toEntity(DocumentoPresentadoRequest request) {
        DocumentoPresentado entity = new DocumentoPresentado();
        entity.setFkIdTipoDocumento(request.tipoId());
        entity.setEntregado(request.entregado() != null ? request.entregado() : false);
        entity.setNombre("");
        entity.setQuienEntrega("");
        entity.setPreparado(false);
        entity.setVence(false);
        if (request.fecha() != null) {
            try {
                entity.setFechaIngreso(DATE_FORMAT.parse(request.fecha()));
            } catch (ParseException e) {
                log.warn("Invalid fecha format: {}", request.fecha());
            }
        }
        return entity;
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
    public ResponseEntity<List<DocumentoPresentadoResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento presentado por ID")
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
