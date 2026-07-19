package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoTipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tipo-de-documento")
@Tag(name = "TipoDeDocumento", description = "API para gestionar tipo-de-documento")
public class TipoDeDocumentoController {

    private final TipoDeDocumentoRepository repository;
    private final PlantillaTramiteRepository plantillaTramiteRepository;
    private final DocumentoPresentadoRepository documentoPresentadoRepository;

    public TipoDeDocumentoController(TipoDeDocumentoRepository repository,
                                     PlantillaTramiteRepository plantillaTramiteRepository,
                                     DocumentoPresentadoRepository documentoPresentadoRepository) {
        this.repository = repository;
        this.plantillaTramiteRepository = plantillaTramiteRepository;
        this.documentoPresentadoRepository = documentoPresentadoRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los tipo-de-documento")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTipoDeDocumento>> getAll() {
        return ResponseEntity.ok(repository.findAll().stream()
                .map(TipoDeDocumento::getDto)
                .toList());
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar tipo-de-documento por nombre")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoTipoDeDocumento>> search(@RequestParam String nombre) {
        return ResponseEntity.ok(repository.findByNombreContaining(nombre).stream()
                .map(TipoDeDocumento::getDto)
                .toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo-de-documento por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoTipoDeDocumento> getById(@PathVariable Integer id) {
        return repository.findById(id)
                .map(e -> ResponseEntity.ok(e.getDto()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/in-use")
    @Operation(summary = "Verificar si el tipo de documento está en uso")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> isInUse(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        boolean inUse = !plantillaTramiteRepository.findByTipoDeDocumentoIdTipoDocumento(id).isEmpty()
                || documentoPresentadoRepository.existsByFkIdTipoDocumento(id);
        return ResponseEntity.ok(Map.of("inUse", inUse));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
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
    @Operation(summary = "Actualizar tipo-de-documento")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody DtoTipoDeDocumento dto) {
        Optional<TipoDeDocumento> existing = repository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!plantillaTramiteRepository.findByTipoDeDocumentoIdTipoDocumento(id).isEmpty()
                || documentoPresentadoRepository.existsByFkIdTipoDocumento(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Este tipo de documento está en uso y no puede modificarse. Cree uno nuevo."));
        }
        try {
            TipoDeDocumento entity = existing.get();
            dto.setIdTipoDocumento(id);
            if (dto.getQuienEntrega() == null) {
                dto.setQuienEntrega(entity.getQuienEntrega());
            }
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
    @Operation(summary = "Eliminar tipo-de-documento")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (!plantillaTramiteRepository.findByTipoDeDocumentoIdTipoDocumento(id).isEmpty()
                || documentoPresentadoRepository.existsByFkIdTipoDocumento(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "No se puede eliminar: el tipo de documento está siendo utilizado en plantillas o documentos presentados."));
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
