package com.licensis.notaire.api;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.DeedFirmaService;
import com.licensis.notaire.service.DeedService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escrituras")
@Tag(name = "Escrituras", description = "API para gestionar escrituras")
public class DeedController {

    private final DeedService deedService;
    private final DeedFirmaService deedFirmaService;
    private final FolioRepository folioRepository;

    public DeedController(DeedService deedService, DeedFirmaService deedFirmaService,
            FolioRepository folioRepository) {
        this.deedService = deedService;
        this.deedFirmaService = deedFirmaService;
        this.folioRepository = folioRepository;
    }

    @GetMapping
    @Operation(summary = "Obtener todas las escrituras")
    @Transactional(readOnly = true)
    public ResponseEntity<Page<Deed>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(deedService.findAllPaged(pageable));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener escritura por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<Deed> getById(@PathVariable Integer id) {
        return deedService.findById(id)
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
    public ResponseEntity<Deed> create(@RequestBody Deed entity) {
        Deed saved = deedService.save(entity);
        linkFolio(saved, entity.getIdFolio());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar escritura")
    @Transactional
    public ResponseEntity<Deed> update(@PathVariable Integer id, @RequestBody Deed entity) {
        return deedService.findById(id)
                .map(existing -> {
                    entity.setIdDeed(id);
                    entity.setVersion(existing.getVersion());
                    Deed updated = deedService.save(entity);
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
        if (deedService.findById(id).isPresent()) {
            deedService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private void linkFolio(Deed deed, Integer idFolio) {
        if (idFolio == null) {
            return;
        }
        folioRepository.findById(idFolio).ifPresent(folio -> {
            folio.setFkIdDeed(deed);
            folioRepository.save(folio);
        });
    }

    @GetMapping("/escribanos-disponibles")
    @Operation(summary = "Obtener lista de escribanos disponibles (con registro)")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Person>> getEscribanosDisponibles() {
        return ResponseEntity.ok(deedService.findEscribanosDisponibles());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar escrituras por numero")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Deed>> searchEscrituras(@RequestParam(required = false) Integer number) {
        return ResponseEntity.ok(deedService.searchPorNumber(number));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "La escritura no está en estado 'Sin Firmar' o no tiene folio asignado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PostMapping("/{id}/firmar")
    @Operation(summary = "Firmar escritura",
               description = "Transiciona una escritura 'Sin Firmar' con folio asignado al estado 'Firmada'")
    public ResponseEntity<Deed> firmar(@PathVariable Integer id) {
        return ResponseEntity.ok(deedFirmaService.firmar(id));
    }
}
