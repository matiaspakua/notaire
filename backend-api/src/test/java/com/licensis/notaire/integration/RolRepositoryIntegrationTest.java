package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Rol;
import com.licensis.notaire.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rol Repository Integration Tests")
class RolRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private RolRepository rolRepository;

    @BeforeEach
    void setUp() {
        rolRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create role with all fields")
    void shouldCreateRoleWithAllFields() {
        Rol rol = new Rol();
        rol.setNombre("EDITOR");
        rol.setDescripcion("Rol de edición");
        rol.setActivo(true);

        Rol saved = rolRepository.save(rol);

        assertThat(saved.getIdRol()).isNotNull();
        assertThat(saved.getNombre()).isEqualTo("EDITOR");
        assertThat(saved.getDescripcion()).isEqualTo("Rol de edición");
        assertThat(saved.isActivo()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve role by ID")
    void shouldRetrieveRoleById() {
        Rol rol = new Rol();
        rol.setNombre("VIEWER");
        rol.setActivo(true);
        Rol saved = rolRepository.save(rol);

        Optional<Rol> found = rolRepository.findById(saved.getIdRol());

        assertThat(found).isPresent();
        assertThat(found.get().getNombre()).isEqualTo("VIEWER");
    }

    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        for (int i = 0; i < 3; i++) {
            Rol rol = new Rol();
            rol.setNombre("ROLE_" + i);
            rol.setActivo(true);
            rolRepository.save(rol);
        }

        List<Rol> all = rolRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should update role")
    void shouldUpdateRole() {
        Rol rol = new Rol();
        rol.setNombre("OLD_NAME");
        rol.setActivo(true);
        Rol saved = rolRepository.save(rol);

        saved.setDescripcion("Descripción actualizada");
        saved.setActivo(false);
        Rol updated = rolRepository.save(saved);

        assertThat(updated.getDescripcion()).isEqualTo("Descripción actualizada");
        assertThat(updated.isActivo()).isFalse();
    }

    @Test
    @DisplayName("Should delete role")
    void shouldDeleteRole() {
        Rol rol = new Rol();
        rol.setNombre("TO_DELETE");
        rol.setActivo(true);
        Rol saved = rolRepository.save(rol);
        Integer id = saved.getIdRol();

        rolRepository.delete(saved);

        Optional<Rol> deleted = rolRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}
