package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Folio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/folio")
@Tag(name = "Folio", description = "API para gestionar folio")
public class FolioController {

    private static final Logger log = LoggerFactory.getLogger(FolioController.class);

    private FolioJpaController getJpaController() {
        return new FolioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los folio")
    public ResponseEntity<List<DtoFolio>> getAll() {
        try {
            // Map to DTOs: serializing raw entities fails because their lazy
            // associations are uninitialized Hibernate proxies.
            List<DtoFolio> result = getJpaController().findFolioEntities().stream()
                    .map(Folio::getDto)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to list folios", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener folio por ID")
    public ResponseEntity<DtoFolio> getById(@PathVariable Integer id) {
        try {
            Folio folio = getJpaController().findFolio(id);
            if (folio == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(folio.getDto());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo folio")
    public ResponseEntity<Object> create(@RequestBody Folio entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            log.error("Failed to create folio", e);
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar folio")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Folio entity) {
        try {
            entity.setIdFolio(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar folio")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
