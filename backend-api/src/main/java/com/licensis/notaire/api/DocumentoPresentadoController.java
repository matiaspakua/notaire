package com.licensis.notaire.api;

import com.licensis.notaire.jpa.DocumentoPresentadoJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.DocumentoPresentado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documento-presentado")
@Tag(name = "DocumentoPresentado", description = "API para gestionar documento-presentado")
public class DocumentoPresentadoController {

    private DocumentoPresentadoJpaController getJpaController() {
        return new DocumentoPresentadoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los documento-presentado")
    public ResponseEntity<List<DocumentoPresentado>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findDocumentoPresentadoEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener documento-presentado por ID")
    public ResponseEntity<DocumentoPresentado> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findDocumentoPresentado(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo documento-presentado")
    public ResponseEntity<Void> create(@RequestBody DocumentoPresentado entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar documento-presentado")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody DocumentoPresentado entity) {
        try {
            entity.setIdDocumentoPresentado(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar documento-presentado")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
