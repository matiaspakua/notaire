package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.service.EscrituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/escrituras")
@Tag(name = "Escrituras", description = "API para gestionar escrituras")
public class EscrituraController {

    private final EscrituraService escrituraService;

    public EscrituraController(EscrituraService escrituraService) {
        this.escrituraService = escrituraService;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las escrituras")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Escritura>> getAll() {
        return ResponseEntity.ok(escrituraService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener escritura por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Escritura> getById(@PathVariable Integer id) {
        return escrituraService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva escritura")
    public ResponseEntity<Escritura> create(@RequestBody Escritura entity) {
        Escritura saved = escrituraService.save(entity);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar escritura")
    public ResponseEntity<Escritura> update(@PathVariable Integer id, @RequestBody Escritura entity) {
        return escrituraService.findById(id)
                .map(existing -> {
                    entity.setIdEscritura(id);
                    entity.setVersion(existing.getVersion());
                    Escritura updated = escrituraService.save(entity);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar escritura")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (escrituraService.findById(id).isPresent()) {
            escrituraService.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/escribanos-disponibles")
    @Operation(summary = "Obtener lista de escribanos disponibles (con registro)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoPersona>> getEscribanosDisponibles() {
        List<Persona> escribanos = escrituraService.findEscribanosDisponibles();
        List<DtoPersona> result = new ArrayList<>();
        for (Persona p : escribanos) {
            result.add(p.getDto());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar escrituras por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Escritura>> buscarEscrituras(@RequestParam(required = false) Integer numero) {
        return ResponseEntity.ok(escrituraService.buscarPorNumero(numero));
    }
}
