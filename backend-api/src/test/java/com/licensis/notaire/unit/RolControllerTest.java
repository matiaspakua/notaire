package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.RolController;
import com.licensis.notaire.negocio.Rol;
import com.licensis.notaire.repository.RolRepository;
import com.licensis.notaire.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RolControllerTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RolController(rolRepository, usuarioRepository)).build();
    }

    private Rol buildRol(Integer id, String nombre) {
        Rol rol = new Rol();
        rol.setIdRol(id);
        rol.setNombre(nombre);
        rol.setDescripcion("Descripción de " + nombre);
        rol.setActivo(true);
        rol.setModulos(List.of("administracion", "usuarios"));
        return rol;
    }

    @Test
    @DisplayName("Should return all roles")
    void shouldReturnAllRoles() throws Exception {
        when(rolRepository.findAll()).thenReturn(List.of(buildRol(1, "ADMIN"), buildRol(2, "OPERADOR")));

        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should return role by ID")
    void shouldReturnRoleById() throws Exception {
        when(rolRepository.findById(1)).thenReturn(Optional.of(buildRol(1, "ADMIN")));

        mockMvc.perform(get("/api/v1/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN"));
    }

    @Test
    @DisplayName("Should return 404 when role not found")
    void shouldReturn404WhenRoleNotFound() throws Exception {
        when(rolRepository.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/roles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should create role and return 201")
    void shouldCreateRole() throws Exception {
        Rol saved = buildRol(1, "ADMIN");
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.empty());
        when(rolRepository.save(any())).thenReturn(saved);

        Map<String, Object> body = Map.of(
                "nombre", "ADMIN",
                "descripcion", "Administrador",
                "activo", true,
                "modulos", List.of("administracion")
        );

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("ADMIN"));
    }

    @Test
    @DisplayName("Should return 409 when role name already exists")
    void shouldReturn409WhenRoleNameExists() throws Exception {
        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(buildRol(1, "ADMIN")));

        Map<String, Object> body = Map.of("nombre", "ADMIN", "activo", true, "modulos", List.of());

        mockMvc.perform(post("/api/v1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should update role and return 200")
    void shouldUpdateRole() throws Exception {
        Rol existing = buildRol(1, "ADMIN");
        when(rolRepository.findById(1)).thenReturn(Optional.of(existing));
        when(rolRepository.save(any())).thenReturn(existing);

        Map<String, Object> body = Map.of(
                "nombre", "ADMIN_UPDATED",
                "descripcion", "Updated",
                "activo", true,
                "modulos", List.of("administracion", "usuarios")
        );

        mockMvc.perform(put("/api/v1/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent role")
    void shouldReturn404WhenUpdatingNonExistentRole() throws Exception {
        when(rolRepository.findById(99)).thenReturn(Optional.empty());

        Map<String, Object> body = Map.of("nombre", "X", "activo", true, "modulos", List.of());

        mockMvc.perform(put("/api/v1/roles/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete role and return 204")
    void shouldDeleteRole() throws Exception {
        when(rolRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent role")
    void shouldReturn404WhenDeletingNonExistentRole() throws Exception {
        when(rolRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/roles/99"))
                .andExpect(status().isNotFound());
    }
}
