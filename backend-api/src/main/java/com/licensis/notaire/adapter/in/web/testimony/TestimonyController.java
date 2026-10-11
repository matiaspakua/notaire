package com.licensis.notaire.adapter.in.web.testimony;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.application.usecase.testimony.TestimonyGenerationVerificationService;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/testimonies")
@Tag(name = "Testimony", description = "API for managing testimony")
public class TestimonyController {

    /**
     * Body of the full update: the same fields as {@link DtoTestimony}, with {@code number},
     * {@code flagged}, {@code verified} and {@code version} required. They map to NOT NULL columns and are
     * primitives in the DTO, so a missing one used to overwrite the stored value with 0/false
     * (issue #655, Owner decision Oct 9). Presence is recorded by the setters Jackson calls.
     * {@code notes} is replaced as sent; an absent {@code deed} keeps the stored link.
     * {@code version} is the one the client read: a different stored version answers 409.
     */
    public static class TestimonyUpdateRequest extends DtoTestimony {
        private boolean numberSent;
        private boolean flaggedSent;
        private boolean verifiedSent;
        private boolean versionSent;

        @Override
        @Schema(description = "Number of the testimony", requiredMode = Schema.RequiredMode.REQUIRED)
        public int getNumber() {
            return super.getNumber();
        }

        @Override
        public void setNumber(int number) {
            numberSent = true;
            super.setNumber(number);
        }

        @Override
        @Schema(description = "Testimony observado", requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean isFlagged() {
            return super.isFlagged();
        }

        @Override
        public void setFlagged(boolean flagged) {
            flaggedSent = true;
            super.setFlagged(flagged);
        }

        @Override
        @Schema(description = "Testimony verificado", requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean isVerified() {
            return super.isVerified();
        }

        @Override
        public void setVerified(boolean verified) {
            verifiedSent = true;
            super.setVerified(verified);
        }

        @Override
        @Schema(description = "Versión leída of the testimony (bloqueo optimista): 409 if already is not the "
                + "almacenada", requiredMode = Schema.RequiredMode.REQUIRED)
        public int getVersion() {
            return super.getVersion();
        }

        @Override
        public void setVersion(int version) {
            versionSent = true;
            super.setVersion(version);
        }

        /** Throws a 400 naming every required field the body did not send. */
        void requireComplete() {
            RequiredFields.check()
                    .present(numberSent, "number")
                    .present(flaggedSent, "flagged")
                    .present(verifiedSent, "verified")
                    .present(versionSent, "version")
                    .orThrow();
        }
    }

    /**
     * Body of the bare create: the same fields as {@link DtoTestimony}, with the deed required.
     * A testimony is a certified copy of a deed (CU07 / CU08), so it cannot exist without one
     * (issue #655). The UI creates testimonies through {@code POST /{idDeed}/generate}.
     */
    public static class TestimonyCreateRequest extends DtoTestimony {
        @Override
        @NotNull
        @Schema(description = "Deed of the that the testimony is copy; `idDeed` must existir (404 if not)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        public DtoDeed getDeed() {
            return super.getDeed();
        }
    }

    private final TestimonyRepository repository;
    private final TestimonyGenerationVerificationService generationVerificacionService;
    private final DeedRepository deedRepository;

    public TestimonyController(TestimonyRepository repository,
            TestimonyGenerationVerificationService generationVerificacionService,
            DeedRepository deedRepository) {
        this.repository = repository;
        this.generationVerificacionService = generationVerificacionService;
        this.deedRepository = deedRepository;
    }

    @GetMapping
    @Operation(summary = "Get all testimony")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTestimony>> getAll() {
        List<DtoTestimony> result = repository.findAll().stream()
                .map(Testimony::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get testimony by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoTestimony> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Invalid request (falta the deed or its idDeed)"),
    @ApiResponse(responseCode = "404", description = "Deed no encontrada"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Create testimony")
    public ResponseEntity<Object> create(@Valid @RequestBody TestimonyCreateRequest dto) {
        Integer idDeed = dto.getDeed().getIdDeed();
        if (idDeed == null) {
            throw new BusinessValidationException("deed.idDeed: es obligatorio");
        }
        if (!deedRepository.existsById(idDeed)) {
            throw new ResourceNotFoundException("Deed no encontrada: " + idDeed);
        }
        try {
            Testimony entity = new Testimony();
            entity.setAttributes(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Cuerpo empty or incompleto: number, flagged, verified and version are "
            + "obligatorios"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the testimony")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update testimony",
               description = "Replaces the testimony. number, flagged, verified and version are obligatorios (400 if "
                       + "falta alguno); notes guarda tal as envía and a deed ausente conserva the actual. "
                       + "version is the leída: if already is not the stored responde 409 (bloqueo optimista).")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody TestimonyUpdateRequest dto) {
        dto.requireComplete();
        Optional<Testimony> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Testimony entity = existing.get();
        if (entity.getVersion() != dto.getVersion()) {
            return ErrorResponses.staleVersion();
        }
        try {
            dto.setIdTestimony(id);
            entity.setAttributes(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ErrorResponses.updateFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete testimony")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Not can delete: the testimony is referenciado by other registros.");
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "The deed is not in status 'Firmada'"),
    @ApiResponse(responseCode = "404", description = "Deed no encontrada")
})
    @PostMapping("/{idDeed}/generate")
    @Operation(summary = "Generate testimony of a deed firmada",
               description = "Generates a testimony with number assigned by the sistema a partir of a deed "
                       + "'Firmada'")
    @Transactional
    public ResponseEntity<DtoTestimony> generate(@PathVariable Integer idDeed) {
        Testimony testimony = generationVerificacionService.generate(idDeed);
        return ResponseEntity.status(HttpStatus.CREATED).body(testimony.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Testimony not found")
})
    @PostMapping("/{id}/verify")
    @Operation(summary = "Check testimony",
               description = "Records the verificación of a testimony, marcando if was observado and by qué")
    @Transactional
    public ResponseEntity<DtoTestimony> verify(@PathVariable Integer id, @RequestBody DtoTestimony dto) {
        Testimony testimony = generationVerificacionService.verify(id, dto.isFlagged(), dto.getNotes());
        return ResponseEntity.ok(testimony.getDto());
    }
}
