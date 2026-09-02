package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoDeFolioRepository;
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
@DisplayName("CU81 — Gestión de trámites en Protocolo Auxiliar")
class ProtocoloAuxiliarControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FolioRepository folioRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoDeFolioRepository tipoDeFolioRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private Persona escribano;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        escribano = personaRepository.findById(1).orElseThrow();
    }

    private TipoDeFolio crearTipoAuxiliar() {
        TipoDeFolio tipo = new TipoDeFolio();
        tipo.setNombre("Protocolo Auxiliar " + System.nanoTime());
        tipo.setEsAuxiliar(true);
        tipo.setHabilitado(true);
        return tipoDeFolioRepository.save(tipo);
    }

    private Folio crearFolioAuxiliar(TipoDeFolio tipo, int numero) {
        Folio folio = new Folio();
        folio.setNumero(numero);
        folio.setAnio(2026);
        folio.setEstado("Nuevo");
        folio.setFkIdTipoFolio(tipo);
        folio.setFkIdPersonaEscribano(escribano);
        return folioRepository.save(folio);
    }

    @Test
    @DisplayName("Should list available auxiliar folios")
    void shouldListAvailableFoliosAuxiliares() throws Exception {
        TipoDeFolio tipo = crearTipoAuxiliar();
        crearFolioAuxiliar(tipo, 501);

        mockMvc.perform(get("/api/v1/protocolo-auxiliar/folios-disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.numero == 501)]").exists());
    }

    @Test
    @DisplayName("Should return an empty list when there are no auxiliar folios available")
    void shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/protocolo-auxiliar/folios-disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasEntry("numero", 999999)))));
    }

    @Test
    @DisplayName("Should create escritura on an available auxiliar folio with its own correlative number")
    void shouldCreateEscrituraOnAvailableFolioAuxiliar() throws Exception {
        TipoDeFolio tipo = crearTipoAuxiliar();
        Folio folio = crearFolioAuxiliar(tipo, 601);

        Map<String, Object> body = new HashMap<>();
        body.put("idFolio", folio.getIdFolio());
        body.put("cuerpo", "Acta de certificación de firma");

        mockMvc.perform(post("/api/v1/protocolo-auxiliar/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").exists());
    }

    @Test
    @DisplayName("Should reject starting an escritura when no auxiliar folio is available")
    void shouldRejectEscrituraWhenNoFolioAuxiliarAvailable() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("cuerpo", "Acta de certificación de firma");

        mockMvc.perform(post("/api/v1/protocolo-auxiliar/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
