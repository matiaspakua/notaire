package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU07", "CU08", "CU12", "CU44"})
@DisplayName("TestimonioController — CU07/CU08 CRUD integration tests")
class TestimonioControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createEscritura() throws Exception {
        String escrituraBody = """
                {
                  "numero": 3001,
                  "cuerpo": "Escritura de prueba para testimonio",
                  "estado": "BORRADOR",
                  "fechaEscrituracion": "2026-06-16"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(escrituraBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idEscritura").asInt();
    }

    private int createEscrituraFirmada() throws Exception {
        String escrituraBody = """
                {
                  "numero": 3002,
                  "cuerpo": "Escritura firmada de prueba para generar testimonio",
                  "estado": "Firmada",
                  "fechaEscrituracion": "2026-06-16"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(escrituraBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idEscritura").asInt();
    }

    // DtoEscritura.numero is primitive int — must be included in nested object to avoid 400
    private String testimonioBody(int numero, boolean observado, int escrituraId) {
        return """
                {
                  "numero": %d,
                  "observado": %s,
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(numero, observado, escrituraId);
    }

    @Test
    @DisplayName("CU07 - Should return 200 and empty array when no testimonios exist")
    void shouldReturnEmptyListWhenNoTestimonios() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CU07 - Should return 404 for non-existing testimonio")
    void shouldReturn404ForNonExistingTestimonio() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should create testimonio linked to escritura and return 201")
    void shouldCreateTestimonioLinkedToEscritura() throws Exception {
        int escrituraId = createEscritura();

        String body = """
                {
                  "numero": 1001,
                  "observado": false,
                  "observaciones": "Testimonio de prueba",
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(escrituraId);

        MvcResult result = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTestimonio").isNumber())
                .andExpect(jsonPath("$.numero").value(1001))
                .andExpect(jsonPath("$.observado").value(false))
                .andReturn();

        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("idTestimonio").asInt()).isPositive();
    }

    @Test
    @DisplayName("CU07 - Should retrieve created testimonio by ID")
    void shouldRetrieveTestimonioById() throws Exception {
        int escrituraId = createEscritura();

        String body = """
                {
                  "numero": 1002,
                  "observado": false,
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(escrituraId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        int id = mapper.readTree(created.getResponse().getContentAsString()).get("idTestimonio").asInt();

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTestimonio").value(id))
                .andExpect(jsonPath("$.numero").value(1002));
    }

    @Test
    @DisplayName("CU08 - Should verify (mark as observado) an existing testimonio")
    void shouldMarkTestimonioAsObservado() throws Exception {
        int escrituraId = createEscritura();

        String createBody = """
                {
                  "numero": 1003,
                  "observado": false,
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(escrituraId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = mapper.readTree(created.getResponse().getContentAsString());
        int id = createdNode.get("idTestimonio").asInt();
        int version = createdNode.get("version").asInt();

        String updateBody = """
                {
                  "idTestimonio": %d,
                  "numero": 1003,
                  "observado": true,
                  "observaciones": "Verificado por escribano",
                  "version": %d,
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(id, version, escrituraId);

        mockMvc.perform(put("/api/v1/testimonio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observado").value(true))
                .andExpect(jsonPath("$.observaciones").value("Verificado por escribano"));
    }

    @Test
    @DisplayName("CU12 - Should delete an existing testimonio")
    void shouldDeleteExistingTestimonio() throws Exception {
        int escrituraId = createEscritura();

        String body = """
                {
                  "numero": 1004,
                  "observado": false,
                  "escritura": {"idEscritura": %d, "numero": 0}
                }
                """.formatted(escrituraId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        int id = mapper.readTree(created.getResponse().getContentAsString()).get("idTestimonio").asInt();

        mockMvc.perform(delete("/api/v1/testimonio/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should return 404 when updating non-existing testimonio")
    void shouldReturn404WhenUpdatingNonExistingTestimonio() throws Exception {
        String body = """
                {"numero": 9999, "observado": false}
                """;
        mockMvc.perform(put("/api/v1/testimonio/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should return 404 when deleting non-existing testimonio")
    void shouldReturn404WhenDeletingNonExistingTestimonio() throws Exception {
        mockMvc.perform(delete("/api/v1/testimonio/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should generate testimonio with system-assigned numero from a Firmada escritura")
    void shouldGenerateTestimonioFromFirmadaEscritura() throws Exception {
        int escrituraId = createEscrituraFirmada();

        mockMvc.perform(post("/api/v1/testimonio/" + escrituraId + "/generar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTestimonio").isNumber())
                .andExpect(jsonPath("$.numero").isNumber())
                .andExpect(jsonPath("$.verificado").value(false))
                .andExpect(jsonPath("$.observado").value(false))
                .andExpect(jsonPath("$.escritura.idEscritura").value(escrituraId));
    }

    @Test
    @DisplayName("CU07 - Should return 400 when generating testimonio from a non-Firmada escritura")
    void shouldRejectGenerateTestimonioFromNonFirmadaEscritura() throws Exception {
        int escrituraId = createEscritura();

        mockMvc.perform(post("/api/v1/testimonio/" + escrituraId + "/generar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CU08 - Should verify a generated testimonio without observaciones")
    void shouldVerifyGeneratedTestimonioWithoutObservaciones() throws Exception {
        int escrituraId = createEscrituraFirmada();

        MvcResult generated = mockMvc.perform(post("/api/v1/testimonio/" + escrituraId + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        int idTestimonio = mapper.readTree(generated.getResponse().getContentAsString())
                .get("idTestimonio").asInt();

        mockMvc.perform(post("/api/v1/testimonio/" + idTestimonio + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observado\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.observado").value(false));
    }

    @Test
    @DisplayName("CU08 - Should verify a generated testimonio with observaciones")
    void shouldVerifyGeneratedTestimonioWithObservaciones() throws Exception {
        int escrituraId = createEscrituraFirmada();

        MvcResult generated = mockMvc.perform(post("/api/v1/testimonio/" + escrituraId + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        int idTestimonio = mapper.readTree(generated.getResponse().getContentAsString())
                .get("idTestimonio").asInt();

        mockMvc.perform(post("/api/v1/testimonio/" + idTestimonio + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observado\": true, \"observaciones\": \"Falta firma del otorgante\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true))
                .andExpect(jsonPath("$.observado").value(true))
                .andExpect(jsonPath("$.observaciones").value("Falta firma del otorgante"));
    }
}
