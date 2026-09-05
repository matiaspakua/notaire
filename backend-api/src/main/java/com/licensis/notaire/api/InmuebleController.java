package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.repository.InmuebleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/inmueble")
@Tag(name = "Inmueble", description = "API para gestionar inmueble")
public class InmuebleController {

    private final InmuebleRepository repository;

    public InmuebleController(InmuebleRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los inmueble")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Inmueble>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inmueble por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Inmueble> getById(@PathVariable Integer id) {
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
    @Operation(summary = "Crear nuevo inmueble")
    public ResponseEntity<Object> create(@RequestBody Inmueble entity) {
        try {
            Inmueble saved = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar inmueble")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Inmueble entity) {
        return repository.findById(id).map(existing -> {
            entity.setIdInmueble(id);
            if (entity.getVersion() == 0 && existing.getVersion() > 0) {
                entity.setVersion(existing.getVersion());
            }
            repository.save(entity);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inmueble")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
