package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.service.EscrituraFirmaService;
import com.licensis.notaire.service.EscrituraService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escrituras")
@Tag(name = "Escrituras", description = "API para gestionar escrituras")
public class EscrituraController {

    private static final Logger log = LoggerFactory.getLogger(EscrituraController.class);

    private final EscrituraService escrituraService;
    private final EscrituraFirmaService escrituraFirmaService;

    public EscrituraController(EscrituraService escrituraService, EscrituraFirmaService escrituraFirmaService) {
        this.escrituraService = escrituraService;
        this.escrituraFirmaService = escrituraFirmaService;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las escrituras")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Escritura>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(escrituraService.findAllPaged(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener escritura por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Escritura> getById(@PathVariable Integer id) {
        return escrituraService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nueva escritura")
    @Transactional
    public ResponseEntity<Escritura> create(@RequestBody Escritura entity) {
        try {
            Escritura saved = escrituraService.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            log.error("Failed to create escritura", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar escritura")
    @Transactional
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

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar escritura")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (escrituraService.findById(id).isPresent()) {
            escrituraService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/escribanos-disponibles")
    @Operation(summary = "Obtener lista de escribanos disponibles (con registro)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Persona>> getEscribanosDisponibles() {
        return ResponseEntity.ok(escrituraService.findEscribanosDisponibles());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar escrituras por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Escritura>> buscarEscrituras(@RequestParam(required = false) Integer numero) {
        return ResponseEntity.ok(escrituraService.buscarPorNumero(numero));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "La escritura no está en estado 'Sin Firmar' o no tiene folio asignado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PostMapping("/{id}/firmar")
    @Operation(summary = "Firmar escritura",
               description = "Transiciona una escritura 'Sin Firmar' con folio asignado al estado 'Firmada'")
    public ResponseEntity<Escritura> firmar(@PathVariable Integer id) {
        return ResponseEntity.ok(escrituraFirmaService.firmar(id));
    }
}
