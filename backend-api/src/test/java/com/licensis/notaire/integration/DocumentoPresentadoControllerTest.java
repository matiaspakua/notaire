package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("DocumentoPresentado controller — create/update/delete via DTO")
class DocumentoPresentadoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private DocumentoPresentadoRepository documentoPresentadoRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createTramite() {
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setNombre("Tramite DocumentoPresentadoControllerTest");
        tipo.setHabilitado(true);
        tipo.setSeArchiva(false);
        tipo.setSeInscribe(false);
        tipo.setAsociaInmuebles(false);
        tipo = tipoDeTramiteRepository.save(tipo);

        Tramite tramite = new Tramite();
        tramite.setFkIdTipoTramite(tipo);
        tramite = tramiteRepository.save(tramite);
        return tramite.getIdTramite();
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with tipoId, fecha and entregado")
    void shouldCreateDocumentoPresentadoWithDtoFields() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-06-01", "entregado": false}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoPresentado").isNumber())
                .andExpect(jsonPath("$.entregado").value(false));
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with entregado true")
    void shouldCreateDocumentoPresentadoEntregado() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-07-15", "entregado": true}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entregado").value(true));
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado linked to a tramite with quienEntrega")
    void shouldCreateDocumentoPresentadoLinkedToTramite() throws Exception {
        Integer tramiteId = createTramite();
        String body = """
                {"tipoId": null, "fecha": "2024-06-01", "entregado": false, "tramiteId": %d,
                 "quienEntrega": "Entidad Externa"}
                """.formatted(tramiteId);

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoPresentado").isNumber())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idDocumentoPresentado").asInt();
        var saved = documentoPresentadoRepository.findById(id).orElseThrow();
        assertThat(saved.getFkIdTramite().getIdTramite()).isEqualTo(tramiteId);
        assertThat(saved.getQuienEntrega()).isEqualTo("Entidad Externa");
    }

    @Test
    @DisplayName("Should return 200 when listing documentos presentados")
    void shouldListDocumentosPresentados() throws Exception {
        mockMvc.perform(get("/api/v1/documento-presentado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 200 and update documento presentado")
    void shouldUpdateDocumentoPresentado() throws Exception {
        String createBody = """
                {"tipoId": null, "fecha": "2024-01-01", "entregado": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = mapper.readTree(response).get("idDocumentoPresentado").asInt();

        String updateBody = """
                {"tipoId": null, "fecha": "2024-12-31", "entregado": true}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent documento presentado")
    void shouldReturn404WhenUpdatingMissingDocumento() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-01-01", "entregado": false}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 and delete an existing documento presentado")
    void shouldDeleteDocumentoPresentado() throws Exception {
        String createBody = """
                {"tipoId": null, "fecha": "2024-05-10", "entregado": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idDocumentoPresentado").asInt();

        mockMvc.perform(delete("/api/v1/documento-presentado/" + id))
                .andExpect(status().isOk());
    }
}
