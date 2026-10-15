package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.user.RoleController;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.CreateRoleCommand;
import com.licensis.notaire.application.port.in.user.ManageRolesUseCase.UpdateRoleCommand;
import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.domain.user.Role;
import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.RoleNotFoundException;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private QueryRolesUseCase query;

    @Mock
    private ManageRolesUseCase manage;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RoleController(query, manage)).build();
    }

    private Role domain(Integer id, String name) {
        return Role.restore(id, name, "Description of " + name, true, List.of("administracion", "users"), 0);
    }

    @Test
    @DisplayName("Should return all roles")
    void shouldReturnAllRoles() throws Exception {
        when(query.findAll()).thenReturn(List.of(domain(1, "ADMIN"), domain(2, "OPERATOR")));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should return role by ID")
    void shouldReturnRoleById() throws Exception {
        when(query.findById(1)).thenReturn(Optional.of(domain(1, "ADMIN")));

        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ADMIN"))
                .andExpect(jsonPath("$.modulos[0]").value("administracion"));
    }

    @Test
    @DisplayName("Should return 404 when role not found")
    void shouldReturn404WhenRoleNotFound() throws Exception {
        when(query.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/roles/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create role and return 201")
    void shouldCreateRole() throws Exception {
        when(manage.create(any(CreateRoleCommand.class))).thenReturn(domain(1, "ADMIN"));

        Map<String, Object> body = Map.of(
                "name", "ADMIN",
                "description", "Administrator",
                "active", true,
                "modulos", List.of("administracion"));

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ADMIN"));
    }

    @Test
    @DisplayName("Should return 409 when role name already exists")
    void shouldReturn409WhenRoleNameExists() throws Exception {
        when(manage.create(any(CreateRoleCommand.class))).thenThrow(new RoleNameConflictException("ADMIN"));

        Map<String, Object> body = Map.of("name", "ADMIN", "active", true, "modulos", List.of());

        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should update role and return 200")
    void shouldUpdateRole() throws Exception {
        when(manage.update(eq(1), any(UpdateRoleCommand.class))).thenReturn(domain(1, "ADMIN_UPDATED"));

        Map<String, Object> body = Map.of(
                "name",
                "ADMIN_UPDATED",
                "description",
                "Updated",
                "active",
                true,
                "modulos",
                List.of("administracion", "users"));

        mockMvc.perform(put("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent role")
    void shouldReturn404WhenUpdatingNonExistentRole() throws Exception {
        when(manage.update(eq(99), any(UpdateRoleCommand.class))).thenThrow(new RoleNotFoundException(99));

        Map<String, Object> body = Map.of("name", "X", "active", true, "modulos", List.of());

        mockMvc.perform(put("/api/v1/roles/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete role and return 204")
    void shouldDeleteRole() throws Exception {
        doNothing().when(manage).delete(1);

        mockMvc.perform(delete("/api/v1/roles/1")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent role")
    void shouldReturn404WhenDeletingNonExistentRole() throws Exception {
        doThrow(new RoleNotFoundException(99)).when(manage).delete(anyInt());

        mockMvc.perform(delete("/api/v1/roles/99")).andExpect(status().isNotFound());
    }
}
