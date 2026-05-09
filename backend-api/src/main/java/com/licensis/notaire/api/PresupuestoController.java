package com.licensis.notaire.api;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.service.PresupuestoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/presupuestos")
@Tag(name = "Presupuestos", description = "API para gestionar presupuestos")
public class PresupuestoController {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoController.class);

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los presupuestos")
    public ResponseEntity<List<Presupuesto>> getAll() {
        return ResponseEntity.ok(presupuestoService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presupuesto por ID")
    public ResponseEntity<Presupuesto> getById(@PathVariable Integer id) {
        return presupuestoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/persona/{idPersona}")
    @Operation(summary = "Obtener presupuestos de una persona (CU60)")
    public ResponseEntity<List<Presupuesto>> getByPersona(@PathVariable Integer idPersona) {
        return ResponseEntity.ok(presupuestoService.findByPersona(idPersona));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar presupuestos por estado (CU60)")
    public ResponseEntity<List<Presupuesto>> buscar(
            @Parameter(description = "Estado del presupuesto") @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(presupuestoService.findByEstado(estado));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo presupuesto")
    public ResponseEntity<Presupuesto> create(@RequestBody Presupuesto entity) {
        Presupuesto saved = presupuestoService.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presupuesto")
    public ResponseEntity<Presupuesto> update(@PathVariable Integer id, @RequestBody Presupuesto entity) {
        try {
            Presupuesto updated = presupuestoService.update(id, entity);
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presupuesto")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            presupuestoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
