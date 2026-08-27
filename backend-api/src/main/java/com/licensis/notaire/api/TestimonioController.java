package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.TestimonioRepository;
import com.licensis.notaire.service.TestimonioGeneracionVerificacionService;
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
public class TestimonioController {

    private final TestimonioRepository repository;
    private final TestimonioGeneracionVerificacionService generacionVerificacionService;

    public TestimonioController(TestimonioRepository repository,
            TestimonioGeneracionVerificacionService generacionVerificacionService) {
        this.repository = repository;
        this.generacionVerificacionService = generacionVerificacionService;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los testimonio")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTestimonio>> getAll() {
        List<DtoTestimonio> result = repository.findAll().stream()
                .map(Testimonio::getDto)
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
    public ResponseEntity<DtoTestimonio> getById(@PathVariable Integer id) {
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
    public ResponseEntity<Object> create(@RequestBody DtoTestimonio dto) {
        try {
            Testimonio entity = new Testimonio();
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
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTestimonio dto) {
        Optional<Testimonio> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Testimonio entity = existing.get();
            dto.setIdTestimonio(id);
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
    @PostMapping("/{idEscritura}/generar")
    @Operation(summary = "Generar testimonio de una escritura firmada",
               description = "Genera un testimonio con número asignado por el sistema a partir de una escritura "
                       + "'Firmada'")
    @Transactional
    public ResponseEntity<DtoTestimonio> generar(@PathVariable Integer idEscritura) {
        Testimonio testimonio = generacionVerificacionService.generar(idEscritura);
        return ResponseEntity.status(HttpStatus.CREATED).body(testimonio.getDto());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "Testimonio no encontrado")
})
    @PostMapping("/{id}/verificar")
    @Operation(summary = "Verificar testimonio",
               description = "Registra la verificación de un testimonio, marcando si fue observado y por qué")
    @Transactional
    public ResponseEntity<DtoTestimonio> verificar(@PathVariable Integer id, @RequestBody DtoTestimonio dto) {
        Testimonio testimonio = generacionVerificacionService.verificar(id, dto.isObservado(), dto.getObservaciones());
        return ResponseEntity.ok(testimonio.getDto());
    }
}
