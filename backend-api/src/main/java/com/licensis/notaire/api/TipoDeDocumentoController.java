package com.licensis.notaire.api;

import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.TipoDeDocumento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-de-documento")
@Tag(name = "TipoDeDocumento", description = "API para gestionar tipo-de-documento")
public class TipoDeDocumentoController {

    private TipoDeDocumentoJpaController getJpaController() {
        return new TipoDeDocumentoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los tipo-de-documento")
    public ResponseEntity<List<TipoDeDocumento>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findTipoDeDocumentoEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo-de-documento por ID")
    public ResponseEntity<TipoDeDocumento> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findTipoDeDocumento(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo-de-documento")
    public ResponseEntity<Void> create(@RequestBody TipoDeDocumento entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo-de-documento")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoDeDocumento entity) {
        try {
            entity.setIdTipoDocumento(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo-de-documento")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
