package com.licensis.notaire.api;

import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-identificacion")
@Tag(name = "TipoIdentificacion", description = "API para gestionar tipos de identificacion")
public class TipoIdentificacionController {

    private static final Logger log = LoggerFactory.getLogger(TipoIdentificacionController.class);

    private final TipoIdentificacionRepository repository;

    public TipoIdentificacionController(TipoIdentificacionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de identificacion")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TipoIdentificacion>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de identificacion por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<TipoIdentificacion> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo tipo de identificacion")
    public ResponseEntity<Object> create(@RequestBody TipoIdentificacion entity) {
        try {
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create tipo de identificacion", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de identificacion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoIdentificacion entity) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            entity.setIdTipoIdentificacion(id);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update tipo de identificacion id {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de identificacion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete tipo de identificacion id {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
