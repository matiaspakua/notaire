package com.licensis.notaire.api;

import com.licensis.notaire.config.JpaControllerProvider;
import com.licensis.notaire.jpa.PlantillaTramiteJpaController;
import com.licensis.notaire.negocio.PlantillaTramite;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plantilla-tramite")
@Tag(name = "PlantillaTramite", description = "API para plantillas de tramite (documentos por tipo de tramite)")
public class PlantillaTramiteController {

    private PlantillaTramiteJpaController getJpaController() {
        return new PlantillaTramiteJpaController(null, JpaControllerProvider.getEntityManagerFactory());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de tramite")
    public ResponseEntity<List<PlantillaTramite>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findPlantillaTramiteEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener plantillas de tramite por tipo de tramite")
    public ResponseEntity<List<PlantillaTramite>> getByTipoTramite(@PathVariable Integer idTipoTramite) {
        try {
            List<PlantillaTramite> list = getJpaController().findPlantillasDeTramite(idTipoTramite);
            return ResponseEntity.ok(list != null ? list : List.of());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
