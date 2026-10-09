package com.licensis.notaire.adapter.in.web.testimony;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.adapter.in.web.support.RequiredFields;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.application.usecase.testimony.TestimonyGenerationVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/testimonio")
@Tag(name = "Testimonio", description = "API para gestionar testimonio")
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
        @Schema(description = "Número del testimonio", requiredMode = Schema.RequiredMode.REQUIRED)
        public int getNumber() {
            return super.getNumber();
        }

        @Override
        public void setNumber(int number) {
            numberSent = true;
            super.setNumber(number);
        }

        @Override
        @Schema(description = "Testimonio observado", requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean isFlagged() {
            return super.isFlagged();
        }

        @Override
        public void setFlagged(boolean flagged) {
            flaggedSent = true;
            super.setFlagged(flagged);
        }

        @Override
        @Schema(description = "Testimonio verificado", requiredMode = Schema.RequiredMode.REQUIRED)
        public boolean isVerified() {
            return super.isVerified();
        }

        @Override
        public void setVerified(boolean verified) {
            verifiedSent = true;
            super.setVerified(verified);
        }

        @Override
        @Schema(description = "Versión leída del testimonio (bloqueo optimista): 409 si ya no es la "
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

    private final TestimonyRepository repository;
    private final TestimonyGenerationVerificationService generationVerificacionService;

    public TestimonyController(TestimonyRepository repository,
            TestimonyGenerationVerificationService generationVerificacionService) {
        this.repository = repository;
        this.generationVerificacionService = generationVerificacionService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los testimonio")
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
    @Operation(summary = "Obtener testimonio por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoTestimony> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo testimonio")
    public ResponseEntity<Object> create(@RequestBody DtoTestimony dto) {
        try {
            Testimony entity = new Testimony();
            entity.setAtributos(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ErrorResponses.conflict(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Cuerpo vacío o incompleto: number, flagged, verified y version son "
            + "obligatorios"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "version no es la almacenada: otro usuario modificó el testimonio")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar testimonio",
               description = "Reemplaza el testimonio. number, flagged, verified y version son obligatorios (400 si "
                       + "falta alguno); notes se guarda tal como se envía y una escritura ausente conserva la actual. "
                       + "version es la leída: si ya no es la almacenada responde 409 (bloqueo optimista).")
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
            entity.setAtributos(dto);
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
    @Operation(summary = "Eliminar testimonio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el testimonio está referenciado por otros registros.");
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "La escritura no está en estado 'Firmada'"),
    @ApiResponse(responseCode = "404", description = "Escritura no encontrada")
})
    @PostMapping("/{idDeed}/generar")
    @Operation(summary = "Generar testimonio de una escritura firmada",
               description = "Genera un testimonio con número asignado por el sistema a partir de una escritura "
                       + "'Firmada'")
    @Transactional
    public ResponseEntity<DtoTestimony> generar(@PathVariable Integer idDeed) {
        Testimony testimony = generationVerificacionService.generate(idDeed);
        return ResponseEntity.status(HttpStatus.CREATED).body(testimony.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{id}/verificar")
    @Operation(summary = "Verificar testimonio",
               description = "Registra la verificación de un testimonio, marcando si fue observado y por qué")
    @Transactional
    public ResponseEntity<DtoTestimony> verificar(@PathVariable Integer id, @RequestBody DtoTestimony dto) {
        Testimony testimony = generationVerificacionService.verify(id, dto.isFlagged(), dto.getNotes());
        return ResponseEntity.ok(testimony.getDto());
    }
}
