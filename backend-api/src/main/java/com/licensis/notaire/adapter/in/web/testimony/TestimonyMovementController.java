package com.licensis.notaire.adapter.in.web.testimony;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.dto.DtoTestimonyMovement;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.application.usecase.testimony.TestimonyMovementService;
import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/testimony-movements")
@Tag(name = "TestimonyMovement", description = "API for managing testimony movement")
public class TestimonyMovementController {

    private final TestimonyMovementRepository repository;
    private final TestimonyMovementService testimonyMovementService;

    public TestimonyMovementController(TestimonyMovementRepository repository,
            TestimonyMovementService testimonyMovementService) {
        this.repository = repository;
        this.testimonyMovementService = testimonyMovementService;
    }

    @GetMapping
    @Operation(summary = "Get all testimony movement")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTestimonyMovement>> getAll() {
        List<DtoTestimonyMovement> result = repository.findAll().stream()
                .map(TestimonyMovement::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Get testimony movement by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoTestimonyMovement> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Create testimony movement")
    public ResponseEntity<Object> create(@RequestBody DtoTestimonyMovement dto) {
        try {
            TestimonyMovement entity = new TestimonyMovement();
            entity.setAtributos(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ErrorResponses.createFailed(e);
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "404", description = "No encontrado"),
    @ApiResponse(responseCode = "409", description = "version is not the stored one: another user modified the record")
})
    @PutMapping("/{id}")
    @Operation(summary = "Update testimony movement")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTestimonyMovement dto) {
        Optional<TestimonyMovement> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        TestimonyMovement entity = existing.get();
        if (entity.getVersion() != dto.getVersion()) {
            return ErrorResponses.staleVersion();
        }
        try {
            dto.setIdTestimonyMovement(id);
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
    @Operation(summary = "Delete testimony movement")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Not can delete: the movimiento of testimony is referenciado by other registros.");
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "The testimony is not verified or already has a movimiento abierto"),
    @ApiResponse(responseCode = "404", description = "Testimony no encontrado")
})
    @PostMapping("/{idTestimony}/enter-registration")
    @Operation(summary = "Enter testimony for registration",
               description = "Records the date of ingreso of a testimony verified to the Registro of the Propiedad")
    public ResponseEntity<DtoTestimonyMovement> registerEntry(@PathVariable Integer idTestimony) {
        TestimonyMovement movement = testimonyMovementService.registerEntry(idTestimony);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "Falta the ingreso a registration previo"),
    @ApiResponse(responseCode = "404", description = "Testimony no encontrado")
})
    @PostMapping("/{idTestimony}/register-inscription")
    @Operation(summary = "Record registration of the testimony",
               description = "Marks as registered the movimiento abierto of a testimony, registrando the date")
    public ResponseEntity<DtoTestimonyMovement> registerInscription(@PathVariable Integer idTestimony) {
        TestimonyMovement movement = testimonyMovementService.registerInscription(idTestimony);
        return ResponseEntity.ok(movement.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "The testimony is not registered"),
    @ApiResponse(responseCode = "404", description = "Testimony no encontrado")
})
    @PostMapping("/{idTestimony}/withdraw")
    @Operation(summary = "Retirar testimony registered",
               description = "Records the date of salida and the number of cartón of a testimony registered")
    public ResponseEntity<DtoTestimonyMovement> withdraw(@PathVariable Integer idTestimony,
            @RequestBody DtoTestimonyMovement dto) {
        TestimonyMovement movement = testimonyMovementService.withdraw(idTestimony, dto.getCardNumber());
        return ResponseEntity.ok(movement.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "The testimony was not retirado previamente, or the reentry observado does not have notes"),
    @ApiResponse(responseCode = "404", description = "Testimony no encontrado")
})
    @PostMapping("/{idTestimony}/reenter")
    @Operation(summary = "Reenter testimony retirado",
               description = "Crea a new movimiento of ingreso for a testimony previamente retirado, without "
                       + "alterar the movimiento anterior. The cuerpo is opcional: number of cartón, if was observado "
                       + "by the registro and notes (obligatorias when was observado)")
    public ResponseEntity<DtoTestimonyMovement> reenter(@PathVariable Integer idTestimony,
            @RequestBody(required = false) DtoTestimonyMovement dto) {
        TestimonyReentry reentry = dto == null ? TestimonyReentry.empty()
                : new TestimonyReentry(dto.getCardNumber(), dto.isObservedByRegistry(), dto.getNotes());
        TestimonyMovement movement = testimonyMovementService.reenter(idTestimony, reentry);
        return ResponseEntity.status(HttpStatus.CREATED).body(movement.getDto());
    }
}
