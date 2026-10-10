package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code GET /api/v1/escrituras?status=} filters the paged deed list on the server
 * (issue #1340, CU06/CU07/CU82). Deed pickers list only signed deeds; without the
 * filter a newest-20 page filtered on the client can be empty while signed deeds exist.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("GET /escrituras?status= filters the paged deed list (issue #1340)")
class DeedStatusFilterIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeedRepository deedRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        save(913401, "Firmada");
        save(913402, "Sin Firmar");
        save(913403, "Firmada");
        save(913404, "Anulada");
    }

    private void save(int number, String deedStatus) {
        Deed deed = new Deed();
        deed.setNumber(number);
        deed.setStatus(deedStatus);
        deed.setDateDeedrecording(new Date());
        deedRepository.save(deed);
    }

    private JsonNode getJson(String url) throws Exception {
        String body = mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return MAPPER.readTree(body);
    }

    @Test
    @DisplayName("shouldReturnOnlyDeedsWithTheRequestedStatusPagedWithTheirTotal")
    void shouldReturnOnlyDeedsWithTheRequestedStatusPagedWithTheirTotal() throws Exception {
        long signed = deedRepository.findByStatus("Firmada").size();
        JsonNode page = getJson("/api/v1/escrituras?status=Firmada&size=1000");
        assertThat(page.path("totalElements").asLong()).isEqualTo(signed);
        assertThat(page.path("content")).isNotEmpty();
        page.path("content").forEach(d -> assertThat(d.path("status").asText()).isEqualTo("Firmada"));
    }

    @Test
    @DisplayName("shouldMatchTheStatusInAnyLetterCase")
    void shouldMatchTheStatusInAnyLetterCase() throws Exception {
        String body = mockMvc.perform(get("/api/v1/escrituras").param("status", "sin firmar").param("size", "1000"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode page = MAPPER.readTree(body);
        assertThat(page.path("content")).isNotEmpty();
        page.path("content").forEach(d -> assertThat(d.path("status").asText()).isEqualTo("Sin Firmar"));
    }

    @Test
    @DisplayName("shouldKeepListingEveryDeedWithoutTheFilter")
    void shouldKeepListingEveryDeedWithoutTheFilter() throws Exception {
        JsonNode page = getJson("/api/v1/escrituras?size=1");
        assertThat(page.path("totalElements").asLong()).isEqualTo(deedRepository.count());
    }

    @Test
    @DisplayName("shouldRejectAnUnknownStatusWith400NamingTheAcceptedValues")
    void shouldRejectAnUnknownStatusWith400NamingTheAcceptedValues() throws Exception {
        String body = mockMvc.perform(get("/api/v1/escrituras?status=Signed"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).contains("Firmada").contains("Sin Firmar");
    }

    @Test
    @DisplayName("shouldDocumentTheOptionalStatusParameterWithItsValues")
    void shouldDocumentTheOptionalStatusParameterWithItsValues() throws Exception {
        JsonNode parameters = getJson("/v3/api-docs").path("paths").path("/api/v1/escrituras").path("get").path("parameters");
        JsonNode statusParam = null;
        for (JsonNode p : parameters) {
            if ("status".equals(p.path("name").asText())) {
                statusParam = p;
            }
        }
        assertThat(statusParam).as("status query parameter").isNotNull();
        assertThat(statusParam.path("required").asBoolean(false)).isFalse();
        assertThat(statusParam.path("schema").path("enum").toString())
                .contains("Sin Firmar", "Firmada", "Anulada", "No Paso", "Inscripta");
    }
}
