package com.licensis.notaire.adapter.in.web.deed;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.application.usecase.deed.DeedSigningService;
import com.licensis.notaire.application.usecase.deed.DeedService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/escrituras")
@Tag(name = "Escrituras", description = "API para gestionar escrituras")
public class DeedController {

    record DeedRequest(
            Integer number,
            String body,
            @NotBlank String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            Integer idFolio) {}

    record DeedResponse(
            Integer idDeed,
            int number,
            String body,
            String status,
            Date dateDeedrecording,
            Date dateRegistration,
            String registrationEntryNumber,
            String notes,
            int version) {}

    private final DeedService deedService;
    private final DeedSigningService deedFirmaService;
    private final FolioRepository folioRepository;

    public DeedController(DeedService deedService, DeedSigningService deedFirmaService,
            FolioRepository folioRepository) {
        this.deedService = deedService;
        this.deedFirmaService = deedFirmaService;
        this.folioRepository = folioRepository;
    }

    private DeedResponse toResponse(Deed deed) {
        return new DeedResponse(
                deed.getIdDeed(),
                deed.getNumber(),
                deed.getBody(),
                deed.getStatus(),
                deed.getDateDeedrecording(),
                deed.getDateRegistration(),
                deed.getRegistrationEntryNumber(),
                deed.getNotes(),
                deed.getVersion());
    }

    private void applyRequest(Deed deed, DeedRequest request) {
        if (request.number() != null) {
            deed.setNumber(request.number());
        }
        deed.setBody(request.body());
        deed.setStatus(request.status());
        deed.setDateDeedrecording(request.dateDeedrecording());
        deed.setDateRegistration(request.dateRegistration());
        deed.setRegistrationEntryNumber(request.registrationEntryNumber());
        deed.setNotes(request.notes());
    }

    @GetMapping
    @Operation(summary = "Obtener todas las escrituras")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DeedResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(deedService.findAllPaged(pageable).map(this::toResponse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener escritura por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DeedResponse> getById(@PathVariable Integer id) {
        return deedService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva escritura")
    @Transactional
    public ResponseEntity<DeedResponse> create(@Valid @RequestBody DeedRequest request) {
        Deed entity = new Deed();
        applyRequest(entity, request);
        Deed saved = deedService.save(entity);
        linkFolio(saved, request.idFolio());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar escritura")
    @Transactional
    public ResponseEntity<DeedResponse> update(@PathVariable Integer id, @Valid @RequestBody DeedRequest request) {
        return deedService.findById(id)
                .map(existing -> {
                    applyRequest(existing, request);
                    Deed updated = deedService.save(existing);
                    return ResponseEntity.ok(toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar escritura")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (deedService.findById(id).isPresent()) {
            deedService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void linkFolio(Deed deed, Integer idFolio) {
        if (idFolio == null) {
            return;
        }
        folioRepository.findById(idFolio).ifPresent(folio -> {
            folio.setFkIdDeed(deed);
            folioRepository.save(folio);
        });
    }

    @GetMapping("/escribanos-disponibles")
    @Operation(summary = "Obtener lista de escribanos disponibles (con registro)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Person>> getEscribanosDisponibles() {
        return ResponseEntity.ok(deedService.findEscribanosDisponibles());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar escrituras por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DeedResponse>> searchDeeds(@RequestParam(required = false) Integer number) {
        return ResponseEntity.ok(deedService.searchPorNumber(number).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "La escritura no está en estado 'Sin Firmar' o no tiene folio asignado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PostMapping("/{id}/firmar")
    @Operation(summary = "Firmar escritura",
               description = "Transiciona una escritura 'Sin Firmar' con folio asignado al estado 'Firmada'")
    public ResponseEntity<DeedResponse> firmar(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(deedFirmaService.sign(id)));
    }
}
