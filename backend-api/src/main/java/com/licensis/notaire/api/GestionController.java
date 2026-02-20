package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.negocio.GestionDeEscritura;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Gestiones", description = "API para gestionar gestiones de escritura")
public class GestionController {

    private GestionDeEscrituraJpaController getJpaController() {
        return new GestionDeEscrituraJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las gestiones")
    public ResponseEntity<List<GestionDeEscritura>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findGestionDeEscrituraEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener gestion por ID")
    public ResponseEntity<GestionDeEscritura> getById(@PathVariable Integer id) {
        try {
            GestionDeEscritura gestion = getJpaController().findGestionDeEscritura(id);
            return gestion != null ? ResponseEntity.ok(gestion) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Obtener gestion por numero")
    public ResponseEntity<GestionDeEscritura> getByNumero(@PathVariable Integer numero) {
        try {
            GestionDeEscritura gestion = getJpaController().findGestionDeEscrituraPorNumero(numero);
            return gestion != null ? ResponseEntity.ok(gestion) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/cliente/{idPersona}")
    @Operation(summary = "Obtener gestiones de un cliente (CU19)")
    public ResponseEntity<List<GestionDeEscritura>> getByCliente(@PathVariable Integer idPersona) {
        try {
            List<GestionDeEscritura> list = getJpaController().findGestionesByCliente(idPersona);
            return ResponseEntity.ok(list != null ? list : List.of());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nueva gestion")
    public ResponseEntity<Void> create(@RequestBody GestionDeEscritura entity) {
        try {
            getJpaController().create(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar gestion")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody GestionDeEscritura entity) {
        try {
            entity.setIdGestion(id);
            getJpaController().edit(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
