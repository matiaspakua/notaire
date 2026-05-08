package com.licensis.notaire.api;

import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeDocumento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-de-documento")
@Tag(name = "TipoDeDocumento", description = "API para gestionar tipo-de-documento")
public class TipoDeDocumentoController {

    private TipoDeDocumentoJpaController getJpaController() {
        return new TipoDeDocumentoJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipo-de-documento")
    public ResponseEntity<List<DtoTipoDeDocumento>> getAll() {
        List<TipoDeDocumento> list = getJpaController().findTipoDeDocumentoEntities();
        List<DtoTipoDeDocumento> result = new ArrayList<>();
        for (TipoDeDocumento e : list) {
            result.add(e.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo-de-documento por ID")
    public ResponseEntity<DtoTipoDeDocumento> getById(@PathVariable Integer id) {
        TipoDeDocumento e = getJpaController().findTipoDeDocumento(id);
        return e != null ? ResponseEntity.ok(e.getDto()) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo-de-documento")
    public ResponseEntity<Object> create(@RequestBody TipoDeDocumento entity) {
        try {
            if (entity.getQuienEntrega() == null) {
                entity.setQuienEntrega("");
            }
            entity.setHabilitado(true);
            entity.setDevuelto(false);
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo-de-documento")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody TipoDeDocumento entity) {
        try {
            entity.setIdTipoDocumento(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo-de-documento")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el tipo de documento está referenciado por otros registros.");
        }
    }
}
