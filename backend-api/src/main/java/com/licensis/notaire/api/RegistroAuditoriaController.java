package com.licensis.notaire.api;

import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/registro-auditoria")
@Tag(name = "RegistroAuditoria", description = "API para consultar y administrar auditoria de usuarios")
public class RegistroAuditoriaController {

    private RegistroAuditoriaJpaController getJpaController() {
        return RegistroAuditoriaJpaController.getInstancia();
    }

    @GetMapping
    @Operation(summary = "Obtener todos los registros de auditoria")
    public ResponseEntity<List<RegistroAuditoria>> getAll() {
        try {
            return ResponseEntity.ok(getJpaController().findRegistroAuditoriaEntities());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener registro de auditoria por ID")
    public ResponseEntity<RegistroAuditoria> getById(@PathVariable Integer id) {
        try {
            RegistroAuditoria registro = getJpaController().findRegistroAuditoria(id);
            return registro != null ? ResponseEntity.ok(registro) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Obtener registros de auditoria por usuario")
    public ResponseEntity<List<RegistroAuditoria>> getByUsuario(@PathVariable Integer idUsuario) {
        try {
            Usuario usuario = new Usuario(idUsuario);
            ArrayList<RegistroAuditoria> registros = getJpaController().buscarRegistroAuditoriasUsuario(usuario);
            if (registros == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(registros);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Crear nuevo registro de auditoria")
    public ResponseEntity<Void> create(@RequestBody RegistroAuditoria entity) {
        try {
            boolean created = getJpaController().create(entity);
            return created ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar registro de auditoria")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            getJpaController().destroy(id);
            return ResponseEntity.ok().build();
        } catch (NonexistentEntityException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
