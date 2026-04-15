package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.negocio.TipoDeFolio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseEntity<List<DtoTipoDeFolio>> getAll() {
        List<TipoDeFolio> list = getJpaController().findTipoDeFolioEntities();
        List<DtoTipoDeFolio> result = new ArrayList<>();
        for (TipoDeFolio e : list) {
            result.add(e.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de folio por ID")
    public ResponseEntity<DtoTipoDeFolio> getById(@PathVariable Integer id) {
        TipoDeFolio e = getJpaController().findTipoDeFolio(id);
        return e != null ? ResponseEntity.ok(e.getDto()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear tipo de folio")
    public ResponseEntity<Object> create(@RequestBody TipoDeFolio tipoDeFolio) {
        try {
            getJpaController().create(tipoDeFolio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de folio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody TipoDeFolio tipoDeFolio) {
        try {
            tipoDeFolio.setIdTipoFolio(id);
            getJpaController().edit(tipoDeFolio);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de folio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el tipo de folio está referenciado por otros registros.");
        }
    }
}
