package com.licensis.notaire.api;

import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movimiento-testimonio")
@Tag(name = "MovimientoTestimonio", description = "API para gestionar movimiento-testimonio")
public class MovimientoTestimonioController {

    private MovimientoTestimonioJpaController getJpaController() {
        return new MovimientoTestimonioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los movimiento-testimonio")
    public ResponseEntity<List<DtoMovimientoTestimonio>> getAll() {
        List<MovimientoTestimonio> list = getJpaController().findMovimientoTestimonioEntities();
        List<DtoMovimientoTestimonio> result = new ArrayList<>();
        for (MovimientoTestimonio e : list) {
            result.add(e.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento-testimonio por ID")
    public ResponseEntity<DtoMovimientoTestimonio> getById(@PathVariable Integer id) {
        MovimientoTestimonio e = getJpaController().findMovimientoTestimonio(id);
        return e != null ? ResponseEntity.ok(e.getDto()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear nuevo movimiento-testimonio")
    public ResponseEntity<Object> create(@RequestBody MovimientoTestimonio entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento-testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody MovimientoTestimonio entity) {
        try {
            entity.setIdMovimientoTestimonio(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento-testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el movimiento de testimonio está referenciado por otros registros.");
        }
    }
}
