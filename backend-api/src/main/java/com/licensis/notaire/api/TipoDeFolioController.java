package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTipoDeFolio;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.TipoDeFolioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tipo-folio")
@Tag(name = "Tipo de Folio", description = "API para tipos de folio")
public class TipoDeFolioController {

    private final TipoDeFolioRepository repository;

    public TipoDeFolioController(TipoDeFolioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de folio")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTipoDeFolio>> getAll() {
        List<DtoTipoDeFolio> result = repository.findAll().stream()
                .map(TipoDeFolio::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de folio por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoTipoDeFolio> getById(@PathVariable Integer id) {
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
    @Operation(summary = "Crear tipo de folio")
    public ResponseEntity<Object> create(@RequestBody DtoTipoDeFolio dto) {
        try {
            TipoDeFolio entity = new TipoDeFolio();
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
    @Operation(summary = "Actualizar tipo de folio")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTipoDeFolio dto) {
        Optional<TipoDeFolio> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            TipoDeFolio entity = existing.get();
            dto.setIdTipoFolio(id);
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
    @Operation(summary = "Eliminar tipo de folio")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el tipo de folio está referenciado por otros registros.");
        }
    }
}
