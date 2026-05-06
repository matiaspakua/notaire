package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gestiones")
@Tag(name = "Gestiones", description = "API para gestionar gestiones de escritura")
public class GestionController {

    private static final Logger log = LoggerFactory.getLogger(GestionController.class);

    private GestionDeEscrituraJpaController getJpaController() {
        return new GestionDeEscrituraJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    private HistorialJpaController getHistorialController() {
        return new HistorialJpaController(null, JpaControllerProvider.getEntityManagerFactory());
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

    @GetMapping("/{id}/estado-actual")
    @Operation(summary = "Obtener estado actual de una gestion")
    public ResponseEntity<Historial> getEstadoActual(@PathVariable Integer id) {
        try {
            List<Historial> historiales = getHistorialController().findRegistroHistial(id);
            if (historiales == null || historiales.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Historial ultimo = historiales.stream()
                    .max(Comparator.comparing(Historial::getFecha))
                    .orElse(null);
            return ResponseEntity.ok(ultimo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/cliente-referencia")
    @Operation(summary = "Obtener cliente referencia de una gestion (primer cliente involucrado)")
    public ResponseEntity<Integer> getClienteReferencia(@PathVariable Integer id) {
        try {
            GestionDeEscritura gestion = getJpaController().findGestionDeEscritura(id);
            if (gestion == null || gestion.getTramiteList() == null || gestion.getTramiteList().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (gestion.getTramiteList().get(0).getPersonaList() != null 
                    && !gestion.getTramiteList().get(0).getPersonaList().isEmpty()) {
                Integer idPersona = gestion.getTramiteList().get(0).getPersonaList().get(0).getIdPersona();
                return ResponseEntity.ok(idPersona);
            }
            return ResponseEntity.notFound().build();
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
            log.error("Failed to create gestion", e);
            e.printStackTrace();
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar gestion")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
