package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("CU39 — Plantilla de costos de documentos por tipo de trámite (Issue #823)")
class PlantillaCostoDocumentoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private TipoDeDocumentoRepository tipoDeDocumentoRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private TipoDeTramite tipoDeTramite;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        tipoDeTramite = tipoDeTramiteRepository.findById(1).orElseThrow();
    }

    private TipoDeDocumento crearTipoDeDocumento(String nombre) {
        TipoDeDocumento tipoDeDocumento = new TipoDeDocumento();
        tipoDeDocumento.setNombre(nombre);
        tipoDeDocumento.setVence(false);
        tipoDeDocumento.setQuienEntrega("Cliente");
        tipoDeDocumento.setDevuelto(false);
        tipoDeDocumento.setHabilitado(true);
        return tipoDeDocumentoRepository.save(tipoDeDocumento);
    }

    private String crearCostoBody(Integer idTipoDocumento, Float montoFijo, Float porcentajeVariable) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("idTipoTramite", tipoDeTramite.getIdTipoTramite());
        body.put("idTipoDocumento", idTipoDocumento);
        body.put("montoFijo", montoFijo);
        body.put("porcentajeVariable", porcentajeVariable);
        return mapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("Should return costos by tipo de tramite")
    void shouldReturnCostosByTipoTramite() throws Exception {
        TipoDeDocumento tipoDeDocumento = crearTipoDeDocumento("Escritura previa");

        mockMvc.perform(post("/api/v1/plantilla-costos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCostoBody(tipoDeDocumento.getIdTipoDocumento(), 2000f, null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/plantilla-costos-documento/tipo-tramite/" + tipoDeTramite.getIdTipoTramite()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].montoFijo").value(2000f));
    }

    @Test
    @DisplayName("Should return empty list when no costos defined")
    void shouldReturnEmptyListWhenNoCostosDefined() throws Exception {
        mockMvc.perform(get("/api/v1/plantilla-costos-documento/tipo-tramite/" + tipoDeTramite.getIdTipoTramite()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
