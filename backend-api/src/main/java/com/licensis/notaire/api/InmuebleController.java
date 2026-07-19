package com.licensis.notaire.api;

import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.negocio.Inmueble;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inmueble")
@Tag(name = "Inmueble", description = "API para gestionar inmueble")
public class InmuebleController {

    private InmuebleJpaController getJpaController() {
        return new InmuebleJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }
    // JpaController instantiated dynamically

    @GetMapping
    @Operation(summary = "Obtener todos los inmueble")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Inmueble>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findInmuebleEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener inmueble por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Inmueble> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(getJpaController().findInmueble(id));
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
    @Operation(summary = "Crear nuevo inmueble")
    public ResponseEntity<Object> create(@RequestBody Inmueble entity) {
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
    @Operation(summary = "Actualizar inmueble")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody Inmueble entity) {
        try {
            entity.setIdInmueble(id);
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
    @Operation(summary = "Eliminar inmueble")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
