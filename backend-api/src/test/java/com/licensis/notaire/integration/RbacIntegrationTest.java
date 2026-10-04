package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.licensis.notaire.business.User;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("RBAC — administrative endpoints require administrator authority (issue #559)")
class RbacIntegrationTest {

    private static final String JSON_BODY = "{\"name\":\"rbac-probe\"}";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private String employeeToken;
    private String administratorToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        employeeToken = tokenForNewUser("EMPLEADO", true);
        administratorToken = tokenForNewUser("Escribano", true);
    }

    private String tokenForNewUser(String type, boolean active) {
        String name = "rbac-" + UUID.randomUUID();
        userRepository.save(new User(null, name, "not-a-login-password", active, type));
        return jwtTokenService.generateToken(name);
    }

    private MockHttpServletRequestBuilder request(HttpMethod method, String path, String token) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .request(method, path)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON_BODY);
    }

    @ParameterizedTest(name = "employee {0} {1} -> 403")
    @CsvSource({
        "GET,/api/v1/usuarios",
        "GET,/api/v1/usuarios/1",
        "PUT,/api/v1/usuarios/1",
        "DELETE,/api/v1/usuarios/999999",
        "GET,/api/v1/roles",
        "POST,/api/v1/roles",
        "GET,/api/v1/audit-log",
        "POST,/api/v1/tipo-tramite",
        "PUT,/api/v1/tipo-de-documento/1",
        "DELETE,/api/v1/conceptos/999999",
        "POST,/api/v1/estado-gestion",
        "POST,/api/v1/workflow-definition",
        "DELETE,/api/v1/workflow-node/999999",
        "PUT,/api/v1/workflow-transition/1",
        "POST,/api/v1/plantilla-tramite",
        "POST,/api/v1/tipo-folio",
        "POST,/api/v1/tipo-identificacion"
    })
    @DisplayName("should forbid an employee on administrative families")
    void shouldForbidAnEmployeeOnAdministrativeFamilies(String method, String path) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), path, employeeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should not create a user when an employee posts an ESCRIBANO account")
    void shouldNotCreateAUserWhenAnEmployeePostsAnEscribanoAccount() throws Exception {
        String name = "escalation-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"password\":\"Test1234!\",\"type\":\"ESCRIBANO\","
                                + "\"active\":true}"))
                .andExpect(status().isForbidden());

        assertThat(userRepository.existsByName(name)).isFalse();
    }

    @ParameterizedTest(name = "employee GET {0} -> 200")
    @CsvSource({
        "/api/v1/tipo-tramite",
        "/api/v1/conceptos",
        "/api/v1/estado-gestion",
        "/api/v1/tipo-de-documento",
        "/api/v1/workflow-definition"
    })
    @DisplayName("should let an employee read catalogs")
    void shouldLetAnEmployeeReadCatalogs(String path) throws Exception {
        mockMvc.perform(get(path).header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should let an administrator list users and roles")
    void shouldLetAnAdministratorListUsersAndRoles() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + administratorToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/roles").header("Authorization", "Bearer " + administratorToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/audit-log").header("Authorization", "Bearer " + administratorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should let an administrator create a tipo de tramite")
    void shouldLetAnAdministratorCreateATipoDeTramite() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-tramite")
                        .header("Authorization", "Bearer " + administratorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"rbac-" + UUID.randomUUID()
                                + "\",\"notes\":\"rbac\",\"isArchived\":false,\"isRegistered\":false}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should reject a token whose user is inactive")
    void shouldRejectATokenWhoseUserIsInactive() throws Exception {
        String inactiveToken = tokenForNewUser("Escribano", false);

        mockMvc.perform(get("/api/v1/tipo-tramite").header("Authorization", "Bearer " + inactiveToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should reject a token whose user no longer exists")
    void shouldRejectATokenWhoseUserNoLongerExists() throws Exception {
        String ghostToken = jwtTokenService.generateToken("ghost-" + UUID.randomUUID());

        mockMvc.perform(get("/api/v1/tipo-tramite").header("Authorization", "Bearer " + ghostToken))
                .andExpect(status().isUnauthorized());
    }
}
