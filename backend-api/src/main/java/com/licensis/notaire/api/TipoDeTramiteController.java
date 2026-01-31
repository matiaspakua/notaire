package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.TipoDeTramiteJpaController;
import com.licensis.notaire.negocio.TipoDeTramite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipo-tramite")
@Tag(name = "Tipo de Tramite", description = "API para tipos de tramite")
public class TipoDeTramiteController {

    private TipoDeTramiteJpaController getJpaController() {
        return new TipoDeTramiteJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de tramite")
    public ResponseEntity<List<TipoDeTramite>> getAll() {
        try {
            List<TipoDeTramite> list = getJpaController().findTipoDeTramiteEntities();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de tramite por ID")
    public ResponseEntity<TipoDeTramite> getById(@PathVariable Integer id) {
        try {
            List<TipoDeTramite> list = getJpaController().findTipoDeTramite(id);
            if (list != null && !list.isEmpty()) {
                return ResponseEntity.ok(list.get(0));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear tipo de tramite")
    public ResponseEntity<Void> create(@RequestBody TipoDeTramite tipoDeTramite) {
        try {
            getJpaController().create(tipoDeTramite);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de tramite")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody TipoDeTramite tipoDeTramite) {
        try {
            tipoDeTramite.setIdTipoTramite(id);
            getJpaController().edit(tipoDeTramite);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de tramite")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
