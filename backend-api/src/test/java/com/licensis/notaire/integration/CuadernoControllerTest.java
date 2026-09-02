package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("CU80 — Cuaderno de folios creation and carátula")
class CuadernoControllerTest {

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
    private TipoDeFolio tipoDeFolio;
    private int siguienteNumero = 1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        escribano = personaRepository.findById(1).orElseThrow();
        tipoDeFolio = tipoDeFolioRepository.findById(1).orElseThrow();
    }

    private List<Integer> crearFolios(int cantidad, String estado) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Folio folio = new Folio();
            folio.setNumero(siguienteNumero++);
            folio.setAnio(2026);
            folio.setEstado(estado);
            folio.setFkIdTipoFolio(tipoDeFolio);
            folio.setFkIdPersonaEscribano(escribano);
            ids.add(folioRepository.save(folio).getIdFolio());
        }
        return ids;
    }

    private String crearCuadernoBody(List<Integer> idsFolio, String observaciones) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("idsFolio", idsFolio);
        body.put("idEscribano", escribano.getIdPersona());
        body.put("anio", 2026);
        body.put("observaciones", observaciones);
        return mapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("Should create cuaderno from ten consecutive folios")
    void shouldCreateCuadernoFromConsecutiveFolios() throws Exception {
        List<Integer> ids = crearFolios(10, "Nuevo");

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(ids, null)))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.numero").exists());
    }

    @Test
    @DisplayName("Should reject cuaderno when folio count is not a multiple of ten")
    void shouldRejectCuadernoWhenFolioCountNotMultipleOfTen() throws Exception {
        List<Integer> ids = crearFolios(5, "Nuevo");

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(ids, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cuaderno with non-consecutive folios")
    void shouldRejectCuadernoWithNonConsecutiveFolios() throws Exception {
        List<Integer> ids = crearFolios(11, "Nuevo");
        List<Integer> discontinuos = new ArrayList<>(ids.subList(0, 9));
        discontinuos.add(ids.get(10));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(discontinuos, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cuaderno with a folio already assigned")
    void shouldRejectCuadernoWithFolioAlreadyAssigned() throws Exception {
        List<Integer> primerLote = crearFolios(10, "Nuevo");
        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(primerLote, null)))
                .andExpect(status().isCreated());

        List<Integer> segundoLote = crearFolios(10, "Nuevo");
        List<Integer> conFolioReasignado = new ArrayList<>(segundoLote.subList(0, 9));
        conFolioReasignado.add(primerLote.get(0));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(conFolioReasignado, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create cuaderno with a justified damaged folio")
    void shouldCreateCuadernoWithJustifiedDamagedFolio() throws Exception {
        List<Integer> ids = crearFolios(9, "Nuevo");
        ids.addAll(crearFolios(1, "Errose"));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(ids, "Folio 10 dañado, se incluye igualmente")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should generate carátula for an existing cuaderno")
    void shouldGenerateCaratulaForExistingCuaderno() throws Exception {
        List<Integer> ids = crearFolios(10, "Nuevo");
        MvcResult creado = mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCuadernoBody(ids, null)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer idCuaderno = mapper.readTree(creado.getResponse().getContentAsString()).get("idCuaderno").asInt();

        MvcResult caratula = mockMvc.perform(get("/api/v1/cuadernos/" + idCuaderno + "/caratula"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(caratula.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(caratula.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 404 for carátula of a missing cuaderno")
    void shouldReturnNotFoundForMissingCuadernoCaratula() throws Exception {
        mockMvc.perform(get("/api/v1/cuadernos/999999/caratula"))
                .andExpect(status().isNotFound());
    }
}
