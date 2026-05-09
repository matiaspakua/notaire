package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tipo-de-documento")
@Tag(name = "TipoDeDocumento", description = "API para gestionar tipo-de-documento")
public class TipoDeDocumentoController {

    private final TipoDeDocumentoRepository repository;

    public TipoDeDocumentoController(TipoDeDocumentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipo-de-documento")
    public ResponseEntity<List<DtoTipoDeDocumento>> getAll() {
        List<DtoTipoDeDocumento> result = repository.findAll().stream()
                .map(TipoDeDocumento::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo-de-documento por ID")
    public ResponseEntity<DtoTipoDeDocumento> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo tipo-de-documento")
    public ResponseEntity<Object> create(@RequestBody DtoTipoDeDocumento dto) {
        try {
            if (dto.getQuienEntrega() == null) {
                dto.setQuienEntrega("");
            }
            dto.setHabilitado(true);
            TipoDeDocumento entity = new TipoDeDocumento();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo-de-documento")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTipoDeDocumento dto) {
        Optional<TipoDeDocumento> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            TipoDeDocumento entity = existing.get();
            dto.setIdTipoDocumento(id);
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo-de-documento")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el tipo de documento está referenciado por otros registros.");
        }
    }
}
