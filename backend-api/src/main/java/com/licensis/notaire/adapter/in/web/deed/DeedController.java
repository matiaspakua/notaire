package com.licensis.notaire.adapter.in.web.deed;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.adapter.in.web.person.PersonController.PersonResponse;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.application.usecase.deed.DeedSigningService;
import com.licensis.notaire.application.usecase.deed.DeedService;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.MediaType;
import com.licensis.notaire.business.BusinessConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/deeds")
@Tag(name = "Deeds", description = "API for managing deeds")
public class DeedController {

    record DeedRequest(
            Integer number,
            String body,
            // Optional: frontend create form omits status; default applied in applyRequest (CU05).
            String status,
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
        String status = request.status();
        if (status == null || status.isBlank()) {
            status = "Sin Firmar";
        }
        deed.setStatus(status);
        deed.setDateDeedrecording(request.dateDeedrecording());
        deed.setDateRegistration(request.dateRegistration());
        deed.setRegistrationEntryNumber(request.registrationEntryNumber());
        deed.setNotes(request.notes());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Status outside the vocabulary", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
})
    @GetMapping
    @Operation(summary = "Get all deeds, optionally only the of a status")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<DeedResponse>> getAll(
            @Parameter(description = "Deed status (any uppercase/lowercase; another valor responde 400). "
                    + "Without this parámetro listan all.",
                    schema = @Schema(allowableValues = {"Sin Firmar", "Firmada", "Anulada", "No Paso", "Inscripta"}))
            @RequestParam(required = false) String status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(deedService.findAllPaged(canonicalStatus(status), pageable).map(this::toResponse));
    }

    /** Deed statuses as stored (BusinessConstants), in workflow order. */
    static final List<String> DEED_STATUSES = List.of(
            BusinessConstants.DeedSINFIRMAR, BusinessConstants.DeedFIRMADA, BusinessConstants.DeedANULADA,
            BusinessConstants.DeedNOPASO, BusinessConstants.DeedRegistered);

    /** The stored spelling of {@code value} in any letter case, null for none, or a 400. */
    static String canonicalStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (String known : DEED_STATUSES) {
            if (known.equalsIgnoreCase(value.trim())) {
                return known;
            }
        }
        throw new BusinessValidationException("status: invalid value '" + value + "'; accepted values: "
                + String.join(", ", DEED_STATUSES));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get deed by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DeedResponse> getById(@PathVariable Integer id) {
        return deedService.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflict")
})
    @PostMapping
    @Operation(summary = "Create deed")
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
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update deed")
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
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete deed")
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

    @GetMapping("/available-notaries")
    @Operation(summary = "Get lista of notaries available (with registro)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PersonResponse>> getAvailableNotaries() {
        return ResponseEntity.ok(deedService.findAvailableNotaries().stream().map(PersonResponse::from).toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Search deeds by number")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DeedResponse>> searchDeeds(@RequestParam(required = false) Integer number) {
        return ResponseEntity.ok(deedService.searchByNumber(number).stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "The deed is not in status 'Sin Firmar' or does not have folio assigned"),
    @ApiResponse(responseCode = "404", description = "Not found")
})
    @PostMapping("/{id}/sign")
    @Operation(summary = "Firmar deed",
               description = "Transitions a deed 'Sin Firmar' with folio assigned to the status 'Firmada'")
    public ResponseEntity<DeedResponse> sign(@PathVariable Integer id) {
        return ResponseEntity.ok(toResponse(deedFirmaService.sign(id)));
    }
}
