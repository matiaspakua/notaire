package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoConcepto;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.repository.ConceptoRepository;
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
@RequestMapping("/api/v1/conceptos")
@Tag(name = "Conceptos", description = "API para gestionar conceptos")
public class ConceptoController {

    private final ConceptoRepository repository;

    public ConceptoController(ConceptoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los conceptos")
    public ResponseEntity<List<DtoConcepto>> getAllConceptos() {
        List<DtoConcepto> result = repository.findAll().stream()
                .map(Concepto::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener concepto por ID")
    public ResponseEntity<DtoConcepto> getConceptoById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo concepto")
    public ResponseEntity<Object> createConcepto(@RequestBody DtoConcepto dto) {
        try {
            dto.setHabilitado(true);
            Concepto entity = new Concepto();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar concepto")
    public ResponseEntity<Object> updateConcepto(@PathVariable Integer id, @RequestBody DtoConcepto dto) {
        Optional<Concepto> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        try {
            dto.setIdConcepto(id);
            Concepto entity = existing.get();
            entity.setAtributos(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar concepto")
    public ResponseEntity<Object> deleteConcepto(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: el concepto está referenciado por otros registros.");
        }
    }
}
