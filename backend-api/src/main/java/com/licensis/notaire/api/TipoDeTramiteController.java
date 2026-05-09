package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tipo-tramite")
@Tag(name = "Tipo de Tramite", description = "API para tipos de tramite")
public class TipoDeTramiteController {

    private final TipoDeTramiteRepository repository;

    public TipoDeTramiteController(TipoDeTramiteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipos de tramite")
    public ResponseEntity<List<DtoTipoDeTramite>> getAll() {
        List<DtoTipoDeTramite> result = repository.findAll().stream()
                .map(TipoDeTramite::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de tramite por ID")
    public ResponseEntity<DtoTipoDeTramite> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear tipo de tramite")
    public ResponseEntity<Object> create(@RequestBody DtoTipoDeTramite dto) {
        try {
            dto.setHabilitado(true);
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            if (dto.getAsociaInmuebles() == null) {
                dto.setAsociaInmuebles(false);
            }
            TipoDeTramite entity = new TipoDeTramite();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de tramite")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTipoDeTramite dto) {
        Optional<TipoDeTramite> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            dto.setIdTipoTramite(id);
            TipoDeTramite entity = existing.get();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de tramite")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el tipo de tramite está referenciado por otros registros.");
        }
    }
}
