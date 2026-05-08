package com.licensis.notaire.api;

import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Presupuesto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/presupuestos")
@Tag(name = "Presupuestos", description = "API para gestionar presupuestos")
public class PresupuestoController {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoController.class);

    private PresupuestoJpaController getJpaController() {
        return new PresupuestoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los presupuestos")
    public ResponseEntity<List<Presupuesto>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findPresupuestoEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID")
    public ResponseEntity<Presupuesto> getById(@PathVariable Integer id) {
        try {
            Presupuesto p = getJpaController().findPresupuesto(id);
            return p != null ? ResponseEntity.ok(p) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/persona/{idPersona}")
    @Operation(summary = "Obtener presupuestos de una persona (CU60)")
    public ResponseEntity<List<Presupuesto>> getByPersona(@PathVariable Integer idPersona) {
        try {
            List<Presupuesto> list = getJpaController().findPresupuestosPersona(idPersona);
            return ResponseEntity.ok(list != null ? list : List.of());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar presupuestos por estado (CU60)")
    public ResponseEntity<List<Presupuesto>> buscar(
            @Parameter(description = "Estado del presupuesto") @RequestParam(required = false) String estado) {
        try {
            List<Presupuesto> all = getJpaController().findPresupuestoEntities();
            if (estado == null || estado.isBlank()) {
                return ResponseEntity.ok(all);
            }
            List<Presupuesto> filtered = all.stream()
                    .filter(p -> estado.equalsIgnoreCase(p.getEstado()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo presupuesto")
    public ResponseEntity<Void> create(@RequestBody Presupuesto entity) {
        try {
            // Set defaults for fields not provided by frontend form
            if (entity.getEncabezado() == null || entity.getEncabezado().isBlank()) {
                entity.setEncabezado("Presupuesto");
            }
            if (entity.getNumero() == 0) {
                entity.setNumero((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
            }
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to create presupuesto", e);
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Presupuesto entity) {
        try {
            entity.setIdPresupuesto(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
