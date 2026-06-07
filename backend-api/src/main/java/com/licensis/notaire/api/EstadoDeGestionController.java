package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoEstadoDeGestion;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/estado-gestion")
@Tag(name = "Estado de Gestion", description = "API para estados de gestion")
public class EstadoDeGestionController {

    private final EstadoDeGestionRepository repository;
    private final GestionDeEscrituraRepository gestionRepository;

    public EstadoDeGestionController(EstadoDeGestionRepository repository,
            GestionDeEscrituraRepository gestionRepository) {
        this.repository = repository;
        this.gestionRepository = gestionRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los estados de gestion")
    public ResponseEntity<List<DtoEstadoDeGestion>> getAll() {
        List<DtoEstadoDeGestion> result = repository.findAll().stream()
                .map(EstadoDeGestion::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener estado de gestion por ID")
    public ResponseEntity<DtoEstadoDeGestion> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar estados de gestion por nombre")
    public ResponseEntity<List<DtoEstadoDeGestion>> search(@RequestParam String nombre) {
        List<DtoEstadoDeGestion> result = repository.findByNombreContaining(nombre).stream()
                .map(EstadoDeGestion::getDto)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si el estado esta referenciado")
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !gestionRepository.findByFkIdEstadoDeGestionIdEstadoGestion(id).isEmpty();
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @PostMapping
    @Operation(summary = "Crear estado de gestion")
    public ResponseEntity<Object> create(@RequestBody DtoEstadoDeGestion dto) {
        try {
            if (dto.getVersion() == null) {
                dto.setVersion(0);
            }
            EstadoDeGestion entity = new EstadoDeGestion();
            entity.setAtributo(dto);
            entity = repository.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(entity.getDto());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar estado de gestion")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoEstadoDeGestion dto) {
        Optional<EstadoDeGestion> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!gestionRepository.findByFkIdEstadoDeGestionIdEstadoGestion(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "No se puede modificar: el estado está referenciado por gestiones de escritura."));
        }
        try {
            EstadoDeGestion entity = existing.get();
            dto.setIdEstadoGestion(id);
            if (dto.getVersion() == null) {
                dto.setVersion(entity.getVersion());
            }
            entity.setAtributo(dto);
            repository.save(entity);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar estado de gestion")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!gestionRepository.findByFkIdEstadoDeGestionIdEstadoGestion(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error",
                            "No se puede eliminar: el estado está referenciado por gestiones de escritura."));
        }
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
