package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("CU34/CU37/CU66 — Concepto referential integrity (issue #432)")
class ConceptoReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createConcepto(String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/conceptos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "%s",
                                  "descripcion": "Test",
                                  "valor": 100,
                                  "habilitado": true,
                                  "porcentaje": 0,
                                  "conceptoFijo": false
                                }
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idConcepto").asInt();
    }

    private void linkConceptoToPlantilla(int tipoTramiteId, int conceptoId) throws Exception {
        mockMvc.perform(post("/api/v1/plantilla-presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plantillaPresupuestoPK": {
                                    "fkIdTipoTramite": %d,
                                    "fkIdConcepto": %d
                                  },
                                  "tipoDeTramite": {"idTipoTramite": %d},
                                  "concepto": {"idConcepto": %d}
                                }
                                """.formatted(tipoTramiteId, conceptoId, tipoTramiteId, conceptoId)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return inUse=false when no plantilla references the concepto")
    void shouldReturnInUseFalseWhenConceptoIsNotReferenced() throws Exception {
        int id = createConcepto("ConceptoFree_InUse");

        mockMvc.perform(get("/api/v1/conceptos/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when a plantilla references the concepto")
    void shouldReturnInUseTrueWhenConceptoIsReferenced() throws Exception {
        int id = createConcepto("ConceptoUsed_InUse");
        linkConceptoToPlantilla(1, id);

        mockMvc.perform(get("/api/v1/conceptos/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing a concepto that is in use")
    void shouldReturn409WhenEditingConceptoInUse() throws Exception {
        int id = createConcepto("ConceptoEditBlocked");
        linkConceptoToPlantilla(1, id);

        mockMvc.perform(put("/api/v1/conceptos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "NombreModificado",
                                  "descripcion": "Updated",
                                  "valor": 200,
                                  "habilitado": true
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing a concepto that is NOT in use")
    void shouldAllowEditingConceptoNotInUse() throws Exception {
        int id = createConcepto("ConceptoEditAllowed");

        mockMvc.perform(put("/api/v1/conceptos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "ConceptoEditAllowed_Updated",
                                  "descripcion": "Updated description",
                                  "valor": 200,
                                  "habilitado": true
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting a concepto that is in use")
    void shouldReturn409WhenDeletingConceptoInUse() throws Exception {
        int id = createConcepto("ConceptoDeleteBlocked");
        linkConceptoToPlantilla(1, id);

        mockMvc.perform(delete("/api/v1/conceptos/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search conceptos by nombre")
    void shouldSearchConceptosByNombre() throws Exception {
        createConcepto("BuscarEsteConcepto");

        mockMvc.perform(get("/api/v1/conceptos/search").param("nombre", "BuscarEste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("BuscarEsteConcepto"));
    }
}
