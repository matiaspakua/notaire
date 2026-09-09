package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
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
@Transactional
public class FolioController {

    private static final Logger log = LoggerFactory.getLogger(FolioController.class);

    private static final String StatusUTILIZADO = "Utilizado";

    record FolioRequest(
            int number,
            int year,
            @NotBlank String status,
            String notes,
            Integer typeFolioId,
            Integer notaryId,
            Integer deedId
    ) {}

    private final FolioRepository folioRepository;
    private final FolioTypeRepository folioTypeRepository;
    private final PersonRepository personRepository;
    private final DeedRepository deedRepository;

    public FolioController(FolioRepository folioRepository,
                           FolioTypeRepository folioTypeRepository,
                           PersonRepository personRepository,
                           DeedRepository deedRepository) {
        this.folioRepository = folioRepository;
        this.folioTypeRepository = folioTypeRepository;
        this.personRepository = personRepository;
        this.deedRepository = deedRepository;
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
    public ResponseEntity<List<DtoFolio>> search(@RequestParam String status) {
        List<DtoFolio> result = folioRepository.findByStatus(status).stream()
                .map(Folio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si un folio está en uso")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        return folioRepository.findById(id)
                .map(f -> ResponseEntity.ok(Map.of("inUse", StatusUTILIZADO.equals(f.getStatus()))))
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
        if (request.typeFolioId() == null || request.notaryId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<FolioType> type = folioTypeRepository.findById(request.typeFolioId());
        Optional<Person> notary = personRepository.findById(request.notaryId());
        if (type.isEmpty() || notary.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Deed deed = null;
        if (request.deedId() != null) {
            Optional<Deed> found = deedRepository.findById(request.deedId());
            if (found.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            deed = found.get();
        }
        try {
            Folio folio = new Folio();
            folio.setNumber(request.number());
            folio.setYear(request.year());
            folio.setStatus(request.status());
            folio.setNotes(request.notes());
            folio.setFkIdFolioType(type.get());
            folio.setFkIdNotaryPerson(notary.get());
            if (deed != null) {
                folio.setFkIdDeed(deed);
                folio.setStatus(StatusUTILIZADO);
            }
            Folio saved = folioRepository.save(folio);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved.getDto());
        } catch (Exception e) {
            log.error("Failed to create folio", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * A folio Utilizado can only be re-saved when the request targets the same
     * escritura it is already linked to (idempotent re-save); any other change
     * while Utilizado is rejected.
     */
    private boolean linksSameDeed(Folio folio, FolioRequest request) {
        Deed linked = folio.getFkIdDeed();
        return linked != null && request.deedId() != null
                && linked.getIdDeed().equals(request.deedId());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar folio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody FolioRequest request) {
        Optional<Folio> existing = folioRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Folio folio = existing.get();
        if (StatusUTILIZADO.equals(folio.getStatus()) && !linksSameDeed(folio, request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este folio está en uso (Utilizado) y no puede modificarse."));
        }
        Deed deed = null;
        if (request.deedId() != null) {
            Optional<Deed> found = deedRepository.findById(request.deedId());
            if (found.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            deed = found.get();
        }
        folio.setNumber(request.number());
        folio.setYear(request.year());
        folio.setStatus(request.status());
        folio.setNotes(request.notes());
        if (request.typeFolioId() != null) {
            folioTypeRepository.findById(request.typeFolioId()).ifPresent(folio::setFkIdFolioType);
        }
        if (request.notaryId() != null) {
            personRepository.findById(request.notaryId()).ifPresent(folio::setFkIdNotaryPerson);
        }
        if (deed != null) {
            folio.setFkIdDeed(deed);
            folio.setStatus(StatusUTILIZADO);
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
        if (StatusUTILIZADO.equals(opt.get().getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el folio está en uso (Utilizado)."));
        }
        try {
            Folio folio = opt.get();
            // TipoDeFolio.folioList is EAGER + CascadeType.ALL: Hibernate 6 would cascade-persist
            // the removed entity back through that collection. Remove it first.
            if (folio.getFkIdFolioType() != null && folio.getFkIdFolioType().getFolioList() != null) {
                folio.getFkIdFolioType().getFolioList().remove(folio);
            }
            folioRepository.delete(folio);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete folio {}", id, e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
