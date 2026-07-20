package com.licensis.notaire.api;

import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.service.RegistroAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/registro-auditoria")
@Tag(name = "RegistroAuditoria", description = "API para consultar y administrar auditoria de usuarios")
public class RegistroAuditoriaController {

    private final RegistroAuditoriaService service;

    public RegistroAuditoriaController(RegistroAuditoriaService service) {
        this.service = service;
    }

    /**
     * Returns audit log entries. Supports pagination, sort and optional
     * filters by {@code modulo} and {@code idUsuario}. When no pagination
     * parameters are present the response is a flat list of DTOs (kept for
     * backwards compatibility with the existing Swing/Next clients).
     */
    @GetMapping
    @Operation(summary = "Obtener los registros de auditoria con paginación y filtros opcionales")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, defaultValue = "fecha,desc") String sort,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) Integer idUsuario
    ) {
        boolean paginated = page != null || size != null;
        if (!paginated) {
            List<DtoRegistroAuditoria> dtos = service.findAllAsDto();
            return ResponseEntity.ok(dtos);
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<DtoRegistroAuditoria> result;
        if (modulo != null && !modulo.isBlank()) {
            result = service.findByModuloAsDto(modulo, pageable);
        } else if (idUsuario != null) {
            result = service.findByUsuarioIdAsDto(idUsuario, pageable);
        } else {
            result = service.findAllAsDto(pageable);
        }
        return ResponseEntity.ok(toPageResponse(result));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de auditoria por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<DtoRegistroAuditoria> getById(@PathVariable Integer id) {
        return service.findByIdAsDto(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener registros de auditoria por usuario")
    @Transactional(readOnly = true)
    public ResponseEntity<List<DtoRegistroAuditoria>> getByUsuario(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(service.findByUsuarioIdAsDto(idUsuario));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo registro de auditoria")
    public ResponseEntity<RegistroAuditoria> create(@RequestBody RegistroAuditoria entity) {
        RegistroAuditoria saved = service.save(entity);
        return ResponseEntity.ok(saved);
    }

    private Pageable buildPageable(Integer page, Integer size, String sort) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        Sort sortSpec = parseSort(sort);
        return PageRequest.of(safePage, safeSize, sortSpec);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "fecha");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1) {
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ex) {
                direction = Sort.Direction.DESC;
            }
        }
        return Sort.by(direction, property);
    }

    private Map<String, Object> toPageResponse(Page<DtoRegistroAuditoria> page) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", page.getContent());
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        response.put("first", page.isFirst());
        response.put("last", page.isLast());
        return response;
    }
}
