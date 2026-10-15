package com.licensis.notaire.adapter.in.web.user;

import java.util.List;
import java.util.Map;

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

import com.licensis.notaire.application.port.in.user.ManageRolesUseCase;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.CreateRoleCommand;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.UpdateRoleCommand;
import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.RoleNotFoundException;
import com.licensis.notaire.domain.user.UserNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Roles. Calls inbound ports only. Module grant lists are stored/returned as today; enforcement is
 * fenced for #1683 (issue #1731).
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "API for managing roles and user permissions")
public class RoleController {

    private final QueryRolesUseCase query;
    private final ManageRolesUseCase manage;

    public RoleController(QueryRolesUseCase query, ManageRolesUseCase manage) {
        this.query = query;
        this.manage = manage;
    }

    /** Wire field {@code modulos} preserves the legacy JSON contract. */
    record RoleResponse(Integer idRole, String name, String description, boolean active, List<String> modulos) {}

    record RoleRequest(@NotBlank String name, String description, boolean active, List<String> modulos) {}

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.id(), role.name(), role.description(), role.active(), role.modules());
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(query.findAll().stream().map(this::toResponse).toList());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Integer id) {
        return query.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Conflict")
    })
    @PostMapping
    @Operation(summary = "Create role")
    public ResponseEntity<Object> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            Role saved = manage.create(
                    new CreateRoleCommand(request.name(), request.description(), request.active(), request.modulos()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (RoleNameConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "409", description = "A role with that name already exists")
    })
    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        try {
            Role saved = manage.update(
                    id,
                    new UpdateRoleCommand(request.name(), request.description(), request.active(), request.modulos()));
            return ResponseEntity.ok(toResponse(saved));
        } catch (RoleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        try {
            manage.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RoleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{idRole}/users/{idUser}")
    @Operation(summary = "Assign role to a user")
    public ResponseEntity<Object> assignRoleToUser(@PathVariable Integer idRole, @PathVariable Integer idUser) {
        try {
            manage.assignRoleToUser(idRole, idUser);
            return ResponseEntity.ok(Map.of("idUser", idUser, "idRole", idRole));
        } catch (RoleNotFoundException | UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Role unassigned"),
        @ApiResponse(responseCode = "404", description = "User account not found")
    })
    @DeleteMapping("/users/{idUser}")
    @Operation(summary = "Unassign role of the user")
    public ResponseEntity<Void> unassignRoleFromUser(@PathVariable Integer idUser) {
        try {
            manage.unassignRoleFromUser(idUser);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
