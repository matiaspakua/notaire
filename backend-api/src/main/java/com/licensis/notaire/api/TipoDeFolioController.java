package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.negocio.TipoDeFolio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-folio")
@Tag(name = "Tipo de Folio", description = "API para tipos de folio")
public class TipoDeFolioController {

    private TipoDeFolioJpaController getJpaController() {
        return new TipoDeFolioJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de folio")
    public ResponseEntity<List<TipoDeFolio>> getAll() {
        try {
            List<TipoDeFolio> list = getJpaController().findTipoDeFolioEntities();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de folio por ID")
    public ResponseEntity<TipoDeFolio> getById(@PathVariable Integer id) {
        try {
            TipoDeFolio e = getJpaController().findTipoDeFolio(id);
            return e != null ? ResponseEntity.ok(e) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear tipo de folio")
    public ResponseEntity<Void> create(@RequestBody TipoDeFolio tipoDeFolio) {
        try {
            getJpaController().create(tipoDeFolio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de folio")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoDeFolio tipoDeFolio) {
        try {
            tipoDeFolio.setIdTipoFolio(id);
            getJpaController().edit(tipoDeFolio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de folio")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
