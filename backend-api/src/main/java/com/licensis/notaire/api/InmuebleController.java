package com.licensis.notaire.api;

import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Inmueble;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inmueble")
@Tag(name = "Inmueble", description = "API para gestionar inmueble")
public class InmuebleController {

    private InmuebleJpaController getJpaController() {
        return new InmuebleJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los inmueble")
    public ResponseEntity<List<Inmueble>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findInmuebleEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener inmueble por ID")
    public ResponseEntity<Inmueble> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findInmueble(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo inmueble")
    public ResponseEntity<Void> create(@RequestBody Inmueble entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar inmueble")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Inmueble entity) {
        try {
            entity.setIdInmueble(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar inmueble")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
