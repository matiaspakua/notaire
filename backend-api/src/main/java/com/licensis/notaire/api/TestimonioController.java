package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.TestimonioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/testimonio")
@Tag(name = "Testimonio", description = "API para gestionar testimonio")
public class TestimonioController {

    private final TestimonioRepository repository;

    public TestimonioController(TestimonioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los testimonio")
    public ResponseEntity<List<DtoTestimonio>> getAll() {
        List<DtoTestimonio> result = repository.findAll().stream()
                .map(Testimonio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener testimonio por ID")
    public ResponseEntity<DtoTestimonio> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo testimonio")
    public ResponseEntity<Object> create(@RequestBody DtoTestimonio dto) {
        try {
            Testimonio entity = new Testimonio();
            entity.setAtributos(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTestimonio dto) {
        Optional<Testimonio> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Testimonio entity = existing.get();
            dto.setIdTestimonio(id);
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el testimonio está referenciado por otros registros.");
        }
    }
}
