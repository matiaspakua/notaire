package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/folio")
@Tag(name = "Folio", description = "API para gestionar folio")
public class FolioController {

    private static final Logger log = LoggerFactory.getLogger(FolioController.class);

    private static final String ESTADO_UTILIZADO = "Utilizado";

    record FolioRequest(
            int numero,
            int anio,
            @NotBlank String estado,
            String observaciones,
            Integer tipoFolioId,
            Integer escribanoId
    ) {}

    private final FolioRepository folioRepository;
    private final TipoDeFolioRepository tipoDeFolioRepository;
    private final PersonaRepository personaRepository;

    public FolioController(FolioRepository folioRepository,
                           TipoDeFolioRepository tipoDeFolioRepository,
                           PersonaRepository personaRepository) {
        this.folioRepository = folioRepository;
        this.tipoDeFolioRepository = tipoDeFolioRepository;
        this.personaRepository = personaRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los folios")
    public ResponseEntity<List<DtoFolio>> getAll() {
        try {
            List<DtoFolio> result = folioRepository.findAll().stream()
                    .map(Folio::getDto)
                    .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to list folios", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar folios por estado")
    public ResponseEntity<List<DtoFolio>> search(@RequestParam String estado) {
        List<DtoFolio> result = folioRepository.findByEstado(estado).stream()
                .map(Folio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si un folio está en uso")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return folioRepository.findById(id)
                .map(f -> ResponseEntity.ok(Map.of("inUse", ESTADO_UTILIZADO.equals(f.getEstado()))))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener folio por ID")
    public ResponseEntity<DtoFolio> getById(@PathVariable Integer id) {
        return folioRepository.findById(id)
                .map(f -> ResponseEntity.ok(f.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo folio")
    public ResponseEntity<DtoFolio> create(@Valid @RequestBody FolioRequest request) {
        if (request.tipoFolioId() == null || request.escribanoId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<TipoDeFolio> tipo = tipoDeFolioRepository.findById(request.tipoFolioId());
        Optional<Persona> escribano = personaRepository.findById(request.escribanoId());
        if (tipo.isEmpty() || escribano.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Folio folio = new Folio();
            folio.setNumero(request.numero());
            folio.setAnio(request.anio());
            folio.setEstado(request.estado());
            folio.setObservaciones(request.observaciones());
            folio.setFkIdTipoFolio(tipo.get());
            folio.setFkIdPersonaEscribano(escribano.get());
            Folio saved = folioRepository.save(folio);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved.getDto());
        } catch (Exception e) {
            log.error("Failed to create folio", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar folio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody FolioRequest request) {
        Optional<Folio> existing = folioRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (ESTADO_UTILIZADO.equals(existing.get().getEstado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este folio está en uso (Utilizado) y no puede modificarse."));
        }
        Folio folio = existing.get();
        folio.setNumero(request.numero());
        folio.setAnio(request.anio());
        folio.setEstado(request.estado());
        folio.setObservaciones(request.observaciones());
        if (request.tipoFolioId() != null) {
            tipoDeFolioRepository.findById(request.tipoFolioId()).ifPresent(folio::setFkIdTipoFolio);
        }
        if (request.escribanoId() != null) {
            personaRepository.findById(request.escribanoId()).ifPresent(folio::setFkIdPersonaEscribano);
        }
        try {
            Folio saved = folioRepository.save(folio);
            return ResponseEntity.ok(saved.getDto());
        } catch (Exception e) {
            log.error("Failed to update folio {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar folio")
    @Transactional
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        Optional<Folio> opt = folioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (ESTADO_UTILIZADO.equals(opt.get().getEstado())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el folio está en uso (Utilizado)."));
        }
        try {
            Folio folio = opt.get();
            // TipoDeFolio.folioList is EAGER + CascadeType.ALL: Hibernate 6 would cascade-persist
            // the removed entity back through that collection. Remove it first.
            if (folio.getFkIdTipoFolio() != null && folio.getFkIdTipoFolio().getFolioList() != null) {
                folio.getFkIdTipoFolio().getFolioList().remove(folio);
            }
            folioRepository.delete(folio);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete folio {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
