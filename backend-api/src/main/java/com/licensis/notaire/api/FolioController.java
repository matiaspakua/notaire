package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/folio")
@Tag(name = "Folio", description = "API para gestionar folio")
public class FolioController {

    private static final Logger log = LoggerFactory.getLogger(FolioController.class);

    record FolioRequest(
            int numero,
            int anio,
            String estado,
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

    @GetMapping("/{id}")
    @Operation(summary = "Obtener folio por ID")
    public ResponseEntity<DtoFolio> getById(@PathVariable Integer id) {
        return folioRepository.findById(id)
                .map(f -> ResponseEntity.ok(f.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo folio")
    public ResponseEntity<DtoFolio> create(@RequestBody FolioRequest request) {
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

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar folio")
    public ResponseEntity<DtoFolio> update(@PathVariable Integer id, @RequestBody FolioRequest request) {
        Optional<Folio> existing = folioRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar folio")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Optional<Folio> opt = folioRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
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
