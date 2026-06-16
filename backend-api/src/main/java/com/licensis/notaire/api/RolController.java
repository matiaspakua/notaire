package com.licensis.notaire.api;

import com.licensis.notaire.negocio.Rol;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RolRepository;
import com.licensis.notaire.repository.UsuarioRepository;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "API para gestionar roles y permisos de usuario")
public class RolController {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    public RolController(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    record RolResponse(Integer idRol, String nombre, String descripcion, boolean activo, List<String> modulos) {}

    record RolRequest(String nombre, String descripcion, boolean activo, List<String> modulos) {}

    private RolResponse toResponse(Rol rol) {
        return new RolResponse(rol.getIdRol(), rol.getNombre(), rol.getDescripcion(),
                rol.isActivo(), rol.getModulos());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los roles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RolResponse>> getAllRoles() {
        return ResponseEntity.ok(rolRepository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<RolResponse> getRolById(@PathVariable Integer id) {
        return rolRepository.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Creado"),
    @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
    @ApiResponse(responseCode = "409", description = "Conflicto")
})
    @PostMapping
    @Operation(summary = "Crear nuevo rol")
    public ResponseEntity<Object> createRol(@RequestBody RolRequest request) {
        if (rolRepository.findByNombre(request.nombre()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya existe un rol con el nombre: " + request.nombre()));
        }
        Rol rol = new Rol();
        rol.setNombre(request.nombre());
        rol.setDescripcion(request.descripcion());
        rol.setActivo(request.activo());
        rol.setModulos(request.modulos() != null ? request.modulos() : List.of());
        Rol saved = rolRepository.save(rol);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<RolResponse> updateRol(@PathVariable Integer id, @RequestBody RolRequest request) {
        Optional<Rol> existing = rolRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Rol rol = existing.get();
        rol.setNombre(request.nombre());
        rol.setDescripcion(request.descripcion());
        rol.setActivo(request.activo());
        rol.setModulos(request.modulos() != null ? request.modulos() : List.of());
        return ResponseEntity.ok(toResponse(rolRepository.save(rol)));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<Void> deleteRol(@PathVariable Integer id) {
        if (!rolRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        rolRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idRol}/usuarios/{idUsuario}")
    @Operation(summary = "Asignar rol a usuario")
    public ResponseEntity<Object> assignRolToUsuario(@PathVariable Integer idRol,
                                                      @PathVariable Integer idUsuario) {
        Optional<Rol> rol = rolRepository.findById(idRol);
        if (rol.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuario.get().setRol(rol.get());
        usuarioRepository.save(usuario.get());
        return ResponseEntity.ok(Map.of("idUsuario", idUsuario, "idRol", idRol));
    }

    @DeleteMapping("/usuarios/{idUsuario}")
    @Operation(summary = "Desasignar rol de usuario")
    public ResponseEntity<Void> unassignRolFromUsuario(@PathVariable Integer idUsuario) {
        Optional<Usuario> usuario = usuarioRepository.findById(idUsuario);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        usuario.get().setRol(null);
        usuarioRepository.save(usuario.get());
        return ResponseEntity.noContent().build();
    }
}
