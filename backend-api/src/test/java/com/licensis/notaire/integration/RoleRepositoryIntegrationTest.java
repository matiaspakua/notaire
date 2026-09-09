package com.licensis.notaire.integration;

import com.licensis.notaire.business.Role;
import com.licensis.notaire.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rol Repository Integration Tests")
class RoleRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create role with all fields")
    void shouldCreateRoleWithAllFields() {
        Role role = new Role();
        role.setName("EDITOR");
        role.setDescription("Rol de edición");
        role.setActive(true);

        Role saved = roleRepository.save(role);

        assertThat(saved.getIdRole()).isNotNull();
        assertThat(saved.getName()).isEqualTo("EDITOR");
        assertThat(saved.getDescription()).isEqualTo("Rol de edición");
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve role by ID")
    void shouldRetrieveRoleById() {
        Role role = new Role();
        role.setName("VIEWER");
        role.setActive(true);
        Role saved = roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(saved.getIdRole());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("VIEWER");
    }

    @Test
    @DisplayName("Should find all roles")
    void shouldFindAllRoles() {
        for (int i = 0; i < 3; i++) {
            Role role = new Role();
            role.setName("ROLE_" + i);
            role.setActive(true);
            roleRepository.save(role);
        }

        List<Role> all = roleRepository.findAll();
        assertThat(all).hasSize(3);
    }

    @Test
    @DisplayName("Should update role")
    void shouldUpdateRole() {
        Role role = new Role();
        role.setName("OLD_NAME");
        role.setActive(true);
        Role saved = roleRepository.save(role);

        saved.setDescription("Descripción actualizada");
        saved.setActive(false);
        Role updated = roleRepository.save(saved);

        assertThat(updated.getDescription()).isEqualTo("Descripción actualizada");
        assertThat(updated.isActive()).isFalse();
    }

    @Test
    @DisplayName("Should delete role")
    void shouldDeleteRole() {
        Role role = new Role();
        role.setName("TO_DELETE");
        role.setActive(true);
        Role saved = roleRepository.save(role);
        Integer id = saved.getIdRole();

        roleRepository.delete(saved);

        Optional<Role> deleted = roleRepository.findById(id);
        assertThat(deleted).isEmpty();
    }
}
