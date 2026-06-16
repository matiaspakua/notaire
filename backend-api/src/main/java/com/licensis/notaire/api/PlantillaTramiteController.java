package com.licensis.notaire.api;

import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
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
@RequestMapping("/api/v1/plantilla-tramite")
@Tag(name = "PlantillaTramite", description = "API para plantillas de tramite (documentos por tipo de tramite)")
public class PlantillaTramiteController {

    private final PlantillaTramiteRepository repository;

    public PlantillaTramiteController(PlantillaTramiteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las plantillas de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlantillaTramite>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/tipo-tramite/{idTipoTramite}")
    @Operation(summary = "Obtener plantillas de tramite por tipo de tramite")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PlantillaTramite>> getByTipoTramite(@PathVariable Integer idTipoTramite) {
        return ResponseEntity.ok(repository.findByTipoDeTramiteIdTipoTramite(idTipoTramite));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{idTipoTramite}/{idTipoDocumento}")
    @Operation(summary = "Obtener plantilla por clave compuesta (CU55)")
    @Transactional(readOnly = true)
    public ResponseEntity<PlantillaTramite> getById(@PathVariable Integer idTipoTramite,
            @PathVariable Integer idTipoDocumento) {
        return repository.findById(new PlantillaTramitePK(idTipoTramite, idTipoDocumento))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    @PostMapping
    @Operation(summary = "Crear plantilla de tramite (CU55)")
    public ResponseEntity<PlantillaTramite> create(@RequestBody PlantillaTramite entity) {
        PlantillaTramite saved = repository.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PutMapping("/{idTipoTramite}/{idTipoDocumento}")
    @Operation(summary = "Actualizar plantilla de tramite (CU55)")
    public ResponseEntity<PlantillaTramite> update(@PathVariable Integer idTipoTramite,
            @PathVariable Integer idTipoDocumento,
            @RequestBody PlantillaTramite entity) {
        PlantillaTramitePK pk = new PlantillaTramitePK(idTipoTramite, idTipoDocumento);
        if (!repository.existsById(pk)) {
            return ResponseEntity.notFound().build();
        }
        entity.setPlantillaTramitePK(pk);
        return ResponseEntity.ok(repository.save(entity));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado"),
        @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @DeleteMapping("/{idTipoTramite}/{idTipoDocumento}")
    @Operation(summary = "Eliminar plantilla de tramite (CU55)")
    public ResponseEntity<Void> delete(@PathVariable Integer idTipoTramite,
            @PathVariable Integer idTipoDocumento) {
        PlantillaTramitePK pk = new PlantillaTramitePK(idTipoTramite, idTipoDocumento);
        if (!repository.existsById(pk)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(pk);
        return ResponseEntity.noContent().build();
    }
}
