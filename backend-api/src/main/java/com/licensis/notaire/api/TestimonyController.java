package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.service.TestimonyGenerationVerificacionService;
import io.swagger.v3.oas.annotations.Operation;
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

    private final TestimonyRepository repository;
    private final TestimonyGenerationVerificacionService generationVerificacionService;

    public TestimonyController(TestimonyRepository repository,
            TestimonyGenerationVerificacionService generationVerificacionService) {
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar testimonio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTestimony dto) {
        Optional<Testimony> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Testimony entity = existing.get();
            dto.setIdTestimony(id);
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
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
            return ResponseEntity.ok().build();
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
        Testimony testimony = generationVerificacionService.generar(idDeed);
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
        Testimony testimony = generationVerificacionService.verificar(id, dto.isFlagged(), dto.getNotes());
        return ResponseEntity.ok(testimony.getDto());
    }
}
