package com.licensis.notaire.api;

import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.config.JpaControllerProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conceptos")
@Tag(name = "Conceptos", description = "API para gestionar conceptos")
public class ConceptoController {

    private ConceptoJpaController getJpaController() {
        return new ConceptoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los conceptos")
    public ResponseEntity<List<Concepto>> getAllConceptos() {
        try {
            List<Concepto> conceptos = getJpaController().findConceptoEntities();
            return ResponseEntity.ok(conceptos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener concepto por ID")
    public ResponseEntity<Concepto> getConceptoById(@PathVariable Integer id) {
        try {
            Concepto concepto = getJpaController().findConcepto(id);
            return ResponseEntity.ok(concepto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo concepto")
    public ResponseEntity<Void> createConcepto(@RequestBody Concepto concepto) {
        try {
            getJpaController().create(concepto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar concepto")
    public ResponseEntity<Void> updateConcepto(@PathVariable Integer id, @RequestBody Concepto concepto) {
        try {
            concepto.setIdConcepto(id);
            getJpaController().edit(concepto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar concepto")
    public ResponseEntity<Void> deleteConcepto(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
