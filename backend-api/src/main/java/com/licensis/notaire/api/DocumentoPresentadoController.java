package com.licensis.notaire.api;

import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private DocumentoPresentadoResponse toResponse(DocumentoPresentado d) {
        TipoDocInfo tipo = null;
        try {
            // getFkIdTipoDocumento() returns primitive int — NPEs when the DB column is NULL
            int tipoId = d.getFkIdTipoDocumento();
            tipo = tipoRepository.findById(tipoId)
                    .map(t -> new TipoDocInfo(t.getIdTipoDocumento(), t.getNombre()))
                    .orElse(null);
        } catch (NullPointerException e) {
            log.debug("fk_id_tipo_documento is NULL for documento {}", d.getIdDocumentoPresentado());
        }
        String fecha = d.getFechaIngreso() != null ? DATE_FORMAT.format(d.getFechaIngreso()) : null;
        return new DocumentoPresentadoResponse(d.getIdDocumentoPresentado(), tipo, fecha, d.getEntregado());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los documentos presentados")
    public ResponseEntity<List<DocumentoPresentadoResponse>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento presentado por ID")
    public ResponseEntity<DocumentoPresentadoResponse> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo documento presentado")
    public ResponseEntity<Object> create(@RequestBody DocumentoPresentado entity) {
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(entity));
        } catch (Exception e) {
            log.error("Failed to create documento presentado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento presentado")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody DocumentoPresentado entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdDocumentoPresentado(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update documento presentado id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

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
