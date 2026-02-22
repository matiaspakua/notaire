package com.licensis.notaire.api;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.service.RegistroAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/registro-auditoria")
@Tag(name = "RegistroAuditoria", description = "API para consultar y administrar auditoria de usuarios")
public class RegistroAuditoriaController {

    private final RegistroAuditoriaService service;

    public RegistroAuditoriaController(RegistroAuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Obtener todos los registros de auditoria")
    public ResponseEntity<List<RegistroAuditoria>> getAll() {
        List<RegistroAuditoria> registros = service.findAll();
        return ResponseEntity.ok(registros);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de auditoria por ID")
    public ResponseEntity<RegistroAuditoria> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener registros de auditoria por usuario")
    public ResponseEntity<List<RegistroAuditoria>> getByUsuario(@PathVariable Integer idUsuario) {
        List<RegistroAuditoria> registros = service.findByUsuarioId(idUsuario);
        return ResponseEntity.ok(registros);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo registro de auditoria")
    public ResponseEntity<RegistroAuditoria> create(@RequestBody RegistroAuditoria entity) {
        RegistroAuditoria saved = service.save(entity);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de auditoria")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
