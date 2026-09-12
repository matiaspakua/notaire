package com.licensis.notaire.api;

import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.RoleRepository;
import com.licensis.notaire.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class RoleController {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleController(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    record RoleResponse(Integer idRole, String name, String description, boolean active, List<String> modulos) {}

    record RoleRequest(@NotBlank String name, String description, boolean active, List<String> modulos) {}

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getIdRole(), role.getName(), role.getDescription(),
                role.isActive(), role.getModulos());
    }

    @GetMapping
    @Operation(summary = "Obtener todos los roles")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Integer id) {
        return roleRepository.findById(id)
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
    public ResponseEntity<Object> createRole(@Valid @RequestBody RoleRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ya existe un rol con el nombre: " + request.name()));
        }
        Role role = new Role();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setActive(request.active());
        role.setModulos(request.modulos() != null ? request.modulos() : List.of());
        Role saved = roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar rol")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        Optional<Role> existing = roleRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Role role = existing.get();
        role.setName(request.name());
        role.setDescription(request.description());
        role.setActive(request.active());
        role.setModulos(request.modulos() != null ? request.modulos() : List.of());
        return ResponseEntity.ok(toResponse(roleRepository.save(role)));
    }

    @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Eliminado"),
    @ApiResponse(responseCode = "404", description = "No encontrado")
})
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{idRole}/usuarios/{idUser}")
    @Operation(summary = "Asignar rol a usuario")
    public ResponseEntity<Object> assignRoleToUser(@PathVariable Integer idRole,
                                                      @PathVariable Integer idUser) {
        Optional<Role> role = roleRepository.findById(idRole);
        if (role.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Optional<User> user = userRepository.findById(idUser);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        user.get().setRole(role.get());
        userRepository.save(user.get());
        return ResponseEntity.ok(Map.of("idUsuario", idUser, "idRol", idRole));
    }

    @DeleteMapping("/usuarios/{idUser}")
    @Operation(summary = "Desasignar rol de usuario")
    public ResponseEntity<Void> unassignRoleFromUser(@PathVariable Integer idUser) {
        Optional<User> user = userRepository.findById(idUser);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        user.get().setRole(null);
        userRepository.save(user.get());
        return ResponseEntity.noContent().build();
    }
}
