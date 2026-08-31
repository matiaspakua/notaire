package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
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
 * CU01 - Preparar Presupuesto, against the real Flyway-managed Postgres
 * schema. {@code PresupuestoController.create}/{@code .update} bind directly
 * to the raw {@code Presupuesto} entity, whose client relation field is
 * {@code fkIdPersona}; the real frontend sends {@code persona} (the DTO's
 * field name), so every Presupuesto created or edited from the UI silently
 * loses its client association (Issue #883).
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Presupuesto - asociación con persona (CU01)")
class PresupuestoPersonaAssociationPgIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona(String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "Cliente IT", "apellido": "CU01", "numeroIdentificacion": "%s",
                 "esCliente": true, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    @Test
    @DisplayName("Should link the Presupuesto to the client when created with the field the frontend sends")
    void shouldPersistPersonaAssociationOnCreate() throws Exception {
        Integer personaId = createPersona("883pg001");
        String body = """
                {"numero": 883001, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg",
                 "estado": "BORRADOR", "persona": {"idPersona": %d}}
                """.formatted(personaId);

        mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.persona.idPersona").value(personaId));
    }

    @Test
    @DisplayName("Should link the Presupuesto to the client when edited to add one")
    void shouldPersistPersonaAssociationOnUpdate() throws Exception {
        Integer personaId = createPersona("883pg002");
        String createBody = """
                {"numero": 883002, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg sin cliente",
                 "estado": "BORRADOR"}
                """;
        MvcResult createResult = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer presupuestoId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idPresupuesto").asInt();

        String updateBody = """
                {"numero": 883002, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg sin cliente",
                 "estado": "BORRADOR", "persona": {"idPersona": %d}}
                """.formatted(personaId);

        mockMvc.perform(put("/api/v1/presupuestos/" + presupuestoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persona.idPersona").value(personaId));
    }

    @Test
    @DisplayName("Should still create a Presupuesto when the client association is omitted")
    void shouldCreateWithoutPersonaWhenOmitted() throws Exception {
        String body = """
                {"numero": 883003, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg opcional",
                 "estado": "BORRADOR"}
                """;

        mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.persona").doesNotExist());
    }

    @Test
    @DisplayName("Should return the client association when reading a single Presupuesto")
    void shouldReturnPersonaFieldOnGetById() throws Exception {
        Integer personaId = createPersona("883pg004");
        String body = """
                {"numero": 883004, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg detalle",
                 "estado": "BORRADOR", "persona": {"idPersona": %d}}
                """.formatted(personaId);
        MvcResult createResult = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer presupuestoId = mapper.readTree(createResult.getResponse().getContentAsString())
                .get("idPresupuesto").asInt();

        mockMvc.perform(get("/api/v1/presupuestos/" + presupuestoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.persona.idPersona").value(personaId));
    }

    @Test
    @DisplayName("Should reflect the presence or absence of the client association per record when listing")
    void shouldReflectPersonaAcrossListedPresupuestos() throws Exception {
        Integer personaId = createPersona("883pg005");
        String withPersonaBody = """
                {"numero": 883005, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg con cliente",
                 "estado": "BORRADOR", "persona": {"idPersona": %d}}
                """.formatted(personaId);
        String withoutPersonaBody = """
                {"numero": 883006, "fecha": "2026-01-01", "encabezado": "Presupuesto CU01 pg sin cliente listado",
                 "estado": "BORRADOR"}
                """;

        MvcResult withPersonaResult = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withPersonaBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer withPersonaId = mapper.readTree(withPersonaResult.getResponse().getContentAsString())
                .get("idPresupuesto").asInt();

        MvcResult withoutPersonaResult = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withoutPersonaBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer withoutPersonaId = mapper.readTree(withoutPersonaResult.getResponse().getContentAsString())
                .get("idPresupuesto").asInt();

        mockMvc.perform(get("/api/v1/presupuestos/" + withPersonaId))
                .andExpect(jsonPath("$.persona.idPersona").value(personaId));
        mockMvc.perform(get("/api/v1/presupuestos/" + withoutPersonaId))
                .andExpect(jsonPath("$.persona").doesNotExist());
    }
}
