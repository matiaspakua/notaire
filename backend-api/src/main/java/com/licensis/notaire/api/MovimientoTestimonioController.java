package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoMovimientoTestimonio;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/movimiento-testimonio")
@Tag(name = "MovimientoTestimonio", description = "API para gestionar movimiento-testimonio")
public class MovimientoTestimonioController {

    private final MovimientoTestimonioRepository repository;

    public MovimientoTestimonioController(MovimientoTestimonioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los movimiento-testimonio")
    public ResponseEntity<List<DtoMovimientoTestimonio>> getAll() {
        List<DtoMovimientoTestimonio> result = repository.findAll().stream()
                .map(MovimientoTestimonio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento-testimonio por ID")
    public ResponseEntity<DtoMovimientoTestimonio> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo movimiento-testimonio")
    public ResponseEntity<Object> create(@RequestBody DtoMovimientoTestimonio dto) {
        try {
            MovimientoTestimonio entity = new MovimientoTestimonio();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento-testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoMovimientoTestimonio dto) {
        Optional<MovimientoTestimonio> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            MovimientoTestimonio entity = existing.get();
            dto.setIdMovimientoTestimonio(id);
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento-testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el movimiento de testimonio está referenciado por otros registros.");
        }
    }
}
