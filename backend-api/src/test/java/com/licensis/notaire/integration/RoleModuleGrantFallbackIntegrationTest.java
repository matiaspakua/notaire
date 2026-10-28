package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.licensis.notaire.business.User;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.UserRepository;
import com.licensis.notaire.security.RoleModuleGrantService;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("Role module grants — template fallback (issue #1683)")
class RoleModuleGrantFallbackIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleModuleGrantService roleModuleGrantService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("should fall back to notary template modules when no role is assigned")
    void shouldFallBackToNotaryTemplateModulesWhenNoRoleIsAssigned() throws Exception {
        String name = "notary-fallback-" + UUID.randomUUID();
        userRepository.save(new User(null, name, "not-a-login-password", true, "Escribano"));

        org.assertj.core.api.Assertions.assertThat(roleModuleGrantService.grantedModules(name)).contains("personas");

        String token = jwtTokenService.generateToken(name);
        mockMvc.perform(get("/api/v1/people").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
