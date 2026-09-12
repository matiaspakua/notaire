package com.licensis.notaire.integration;

import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.RoleRepository;
import com.licensis.notaire.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario Repository Integration Tests")
class UserRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        roleAdmin = new Role();
        roleAdmin.setName("ADMIN");
        roleAdmin.setActive(true);
        roleAdmin = roleRepository.save(roleAdmin);
    }

    @Test
    @DisplayName("Should create user with role")
    void shouldCreateUserWithRole() {
        User user = new User();
        user.setName("testuser");
        user.setPassword("securepass");
        user.setStatus(true);
        user.setType("ADMIN");
        user.setRole(roleAdmin);

        User saved = userRepository.save(user);

        assertThat(saved.getIdUser()).isNotNull();
        assertThat(saved.getName()).isEqualTo("testuser");
        assertThat(saved.getRole().getIdRole()).isEqualTo(roleAdmin.getIdRole());
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void shouldRetrieveUserById() {
        User user = new User();
        user.setName("user123");
        user.setPassword("pass123");
        user.setStatus(true);
        user.setType("USER");
        user.setRole(roleAdmin);
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getIdUser());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("user123");
    }

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
        User user = new User();
        user.setName("statususer");
        user.setPassword("pass");
        user.setStatus(true);
        user.setType("USER");
        user.setRole(roleAdmin);
        User saved = userRepository.save(user);

        saved.setStatus(false);
        User updated = userRepository.save(saved);

        assertThat(updated.getStatus()).isFalse();
    }

    @Test
    @DisplayName("Should support multiple users")
    void shouldSupportMultipleUsers() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setName("user" + i);
            user.setPassword("pass" + i);
            user.setStatus(true);
            user.setType("USER");
            user.setRole(roleAdmin);
            userRepository.save(user);
        }

        List<User> all = userRepository.findAll();
        assertThat(all).hasSize(5);
    }
}
