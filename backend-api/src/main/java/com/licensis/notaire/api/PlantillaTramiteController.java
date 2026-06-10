package com.licensis.notaire.api;

import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plantilla-tramite")
@Tag(name = "PlantillaTramite", description = "API para plantillas de tramite (documentos por tipo de tramite)")
public class PlantillaTramiteController {

    private final PlantillaTramiteRepository repository;

    public PlantillaTramiteController(PlantillaTramiteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de tramite")
    public ResponseEntity<List<PlantillaTramite>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener plantillas de tramite por tipo de tramite")
    public ResponseEntity<List<PlantillaTramite>> getByTipoTramite(@PathVariable Integer idTipoTramite) {
        return ResponseEntity.ok(repository.findByTipoDeTramiteIdTipoTramite(idTipoTramite));
    }
}
