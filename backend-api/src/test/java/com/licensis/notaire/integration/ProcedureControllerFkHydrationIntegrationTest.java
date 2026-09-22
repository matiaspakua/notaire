package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Reproduces issue #981: {@code POST}/{@code PUT /api/v1/tramites} must resolve
 * plain FK ids (idDeed, idProperty, idManagement, idBudget, idProcedureType)
 * against their real persisted rows, never trust a client-supplied nested
 * object as-is. See openspec/changes/fix-procedure-nested-fk-hydration/.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("CU82 — Procedure FK hydration on create/update (issue #981)")
class ProcedureControllerFkHydrationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DeedRepository deedRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createProcedureType() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Tipo FK Hydration", "isRegistered": false,
                                 "isArchived": false, "associatesProperties": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }

    private Integer createSignedDeed(int number) {
        Deed deed = new Deed();
        deed.setNumber(number);
        deed.setDateDeedrecording(new Date());
        deed.setBody("Cuerpo de escritura de prueba");
        deed.setStatus("Firmada");
        deed = deedRepository.save(deed);
        return deed.getIdDeed();
    }

    @Test
    @DisplayName("POST /api/v1/tramites with a real idDeed returns the Deed's actual state")
    void shouldReturnRealDeedStateOnCreate() throws Exception {
        Integer typeId = createProcedureType();
        Integer deedId = createSignedDeed(777);

        MvcResult result = mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d, "idDeed": %d}
                                """.formatted(typeId, deedId)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode fkIdDeed = body.get("fkIdDeed");
        assertThat(fkIdDeed).as("response should embed fkIdDeed").isNotNull();
        assertThat(fkIdDeed.get("status").asText()).isEqualTo("Firmada");
        assertThat(fkIdDeed.get("number").asInt()).isEqualTo(777);
    }

    @Test
    @DisplayName("GET /api/v1/tramites/{id} after creation reflects the same real Deed state")
    void shouldReflectRealDeedStateOnSubsequentGet() throws Exception {
        Integer typeId = createProcedureType();
        Integer deedId = createSignedDeed(778);

        MvcResult createResult = mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d, "idDeed": %d}
                                """.formatted(typeId, deedId)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer procedureId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idProcedure").asInt();

        MvcResult getResult = mockMvc.perform(get("/api/v1/tramites/" + procedureId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode fkIdDeed = mapper.readTree(getResult.getResponse().getContentAsString()).get("fkIdDeed");
        assertThat(fkIdDeed.get("status").asText()).isEqualTo("Firmada");
        assertThat(fkIdDeed.get("number").asInt()).isEqualTo(778);
    }

    @Test
    @DisplayName("POST /api/v1/tramites without idProcedureType returns 400")
    void shouldRejectMissingProcedureType() throws Exception {
        mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/tramites with a non-existent idDeed returns 404")
    void shouldRejectNonExistentDeed() throws Exception {
        Integer typeId = createProcedureType();

        mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d, "idDeed": 999999}
                                """.formatted(typeId)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/tramites/{id} with a real idDeed returns the Deed's actual state")
    void shouldReturnRealDeedStateOnUpdate() throws Exception {
        Integer typeId = createProcedureType();

        MvcResult createResult = mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d}
                                """.formatted(typeId)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer procedureId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idProcedure").asInt();

        Integer deedId = createSignedDeed(779);

        MvcResult updateResult = mockMvc.perform(put("/api/v1/tramites/" + procedureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d, "idDeed": %d}
                                """.formatted(typeId, deedId)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode fkIdDeed = mapper.readTree(updateResult.getResponse().getContentAsString()).get("fkIdDeed");
        assertThat(fkIdDeed).as("update response should embed fkIdDeed").isNotNull();
        assertThat(fkIdDeed.get("status").asText()).isEqualTo("Firmada");
        assertThat(fkIdDeed.get("number").asInt()).isEqualTo(779);
    }
}
