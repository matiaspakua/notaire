package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Paged list endpoints document the paging query parameters clients really send:
 * optional {@code page}, {@code size} and {@code sort}, instead of a single required
 * {@code pageable} object that no client can satisfy as written (issue #596).
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Paged endpoints document optional page/size/sort (issue #596)")
class PagedEndpointsOpenApiParametersIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @ParameterizedTest(name = "GET {0}")
    @ValueSource(strings = {
        "/api/v1/people",
        "/api/v1/presupuestos",
        "/api/v1/escrituras",
        "/api/v1/tramites",
        "/api/v1/gestiones",
        "/api/v1/historial",
        "/api/v1/audit-log"
    })
    @DisplayName("shouldDocumentOptionalPageSizeAndSort")
    void shouldDocumentOptionalPageSizeAndSort(String path) throws Exception {
        String json = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode parameters = new ObjectMapper().readTree(json)
                .path("paths").path(path).path("get").path("parameters");

        Map<String, JsonNode> byName = new HashMap<>();
        parameters.forEach(parameter -> byName.put(parameter.path("name").asText(), parameter));

        assertThat(byName).as("parameters of GET %s", path).doesNotContainKey("pageable");
        assertThat(byName).as("parameters of GET %s", path).containsKeys("page", "size", "sort");
        for (String name : new String[] {"page", "size", "sort"}) {
            JsonNode parameter = byName.get(name);
            assertThat(parameter.path("in").asText()).as("%s of GET %s", name, path).isEqualTo("query");
            assertThat(parameter.path("required").asBoolean(false)).as("%s of GET %s", name, path).isFalse();
        }
    }
}
