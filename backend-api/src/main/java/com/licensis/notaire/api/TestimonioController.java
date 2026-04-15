package com.licensis.notaire.api;

import com.licensis.notaire.jpa.TestimonioJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/testimonio")
@Tag(name = "Testimonio", description = "API para gestionar testimonio")
public class TestimonioController {

    private TestimonioJpaController getJpaController() {
        return new TestimonioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los testimonio")
    public ResponseEntity<List<DtoTestimonio>> getAll() {
        List<Testimonio> list = getJpaController().findTestimonioEntities();
        List<DtoTestimonio> result = new ArrayList<>();
        for (Testimonio e : list) {
            result.add(e.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener testimonio por ID")
    public ResponseEntity<DtoTestimonio> getById(@PathVariable Integer id) {
        Testimonio e = getJpaController().findTestimonio(id);
        return e != null ? ResponseEntity.ok(e.getDto()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear nuevo testimonio")
    public ResponseEntity<Object> create(@RequestBody Testimonio entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody Testimonio entity) {
        try {
            entity.setIdTestimonio(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el testimonio está referenciado por otros registros.");
        }
    }
}
