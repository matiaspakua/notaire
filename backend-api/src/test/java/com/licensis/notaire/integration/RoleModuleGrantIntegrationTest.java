package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.licensis.notaire.business.Role;
import com.licensis.notaire.business.User;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.RoleRepository;
import com.licensis.notaire.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("Role module grants — server-side enforcement (issue #1683)")
class RoleModuleGrantIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("should allow payments API when the role grants presupuestos")
    void shouldAllowPaymentsApiWhenTheRoleGrantsPresupuestos() throws Exception {
        String token = tokenForUserWithRoleModules(List.of("presupuestos"));

        mockMvc.perform(get("/api/v1/payments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should deny payments API with 403 when presupuestos is missing from the role")
    void shouldDenyPaymentsApiWhenPresupuestosIsMissingFromTheRole() throws Exception {
        String token = tokenForUserWithRoleModules(List.of("personas"));

        mockMvc.perform(get("/api/v1/payments").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String tokenForUserWithRoleModules(List<String> modules) {
        Role role = new Role();
        role.setName("grant-probe-" + UUID.randomUUID());
        role.setDescription("test");
        role.setActive(true);
        role.setModulos(modules);
        role = roleRepository.save(role);

        String username = "grant-user-" + UUID.randomUUID();
        User user = new User(null, username, "not-a-login-password", true, "EMPLEADO");
        user.setRole(role);
        userRepository.save(user);

        return jwtTokenService.generateToken(username);
    }
}
