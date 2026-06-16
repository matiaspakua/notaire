package com.licensis.notaire.api;

import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Suplencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suplencia")
@Tag(name = "Suplencia", description = "API para gestionar suplencia")
public class SuplenciaController {

    private SuplenciaJpaController getJpaController() {
        return new SuplenciaJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los suplencia")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Suplencia>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findSuplenciaEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener suplencia por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Suplencia> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findSuplencia(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo suplencia")
    public ResponseEntity<Object> create(@RequestBody Suplencia entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar suplencia")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Suplencia entity) {
        try {
            entity.setIdSuplencia(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar suplencia")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
