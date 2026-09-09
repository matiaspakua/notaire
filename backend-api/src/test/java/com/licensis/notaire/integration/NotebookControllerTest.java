package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
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
class NotebookControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private FolioRepository folioRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private FolioTypeRepository folioTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private Person notary;
    private FolioType folioType;
    private int siguienteNumber = 1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        notary = personRepository.findById(1).orElseThrow();
        folioType = folioTypeRepository.findById(1).orElseThrow();
    }

    private List<Integer> crearFolios(int cantidad, String status) {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Folio folio = new Folio();
            folio.setNumber(siguienteNumber++);
            folio.setYear(2026);
            folio.setStatus(status);
            folio.setFkIdFolioType(folioType);
            folio.setFkIdNotaryPerson(notary);
            ids.add(folioRepository.save(folio).getIdFolio());
        }
        return ids;
    }

    private String createNotebookBody(List<Integer> idsFolio, String notes) throws Exception {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("idsFolio", idsFolio);
        body.put("idNotary", notary.getPersonId());
        body.put("year", 2026);
        body.put("notes", notes);
        return mapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("Should create cuaderno from ten consecutive folios")
    void shouldCreateNotebookFromConsecutiveFolios() throws Exception {
        List<Integer> ids = crearFolios(10, "Nuevo");

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.number").exists());
    }

    @Test
    @DisplayName("Should reject cuaderno when folio count is not a multiple of ten")
    void shouldRejectNotebookWhenFolioCountNotMultipleOfTen() throws Exception {
        List<Integer> ids = crearFolios(5, "Nuevo");

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cuaderno with non-consecutive folios")
    void shouldRejectNotebookWithNonConsecutiveFolios() throws Exception {
        List<Integer> ids = crearFolios(11, "Nuevo");
        List<Integer> discontinuos = new ArrayList<>(ids.subList(0, 9));
        discontinuos.add(ids.get(10));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(discontinuos, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject cuaderno with a folio already assigned")
    void shouldRejectNotebookWithFolioAlreadyAssigned() throws Exception {
        List<Integer> primerLote = crearFolios(10, "Nuevo");
        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(primerLote, null)))
                .andExpect(status().isCreated());

        List<Integer> segundoLote = crearFolios(10, "Nuevo");
        List<Integer> conFolioReasignado = new ArrayList<>(segundoLote.subList(0, 9));
        conFolioReasignado.add(primerLote.get(0));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(conFolioReasignado, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create cuaderno with a justified damaged folio")
    void shouldCreateNotebookWithJustifiedDamagedFolio() throws Exception {
        List<Integer> ids = crearFolios(9, "Nuevo");
        ids.addAll(crearFolios(1, "Errose"));

        mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, "Folio 10 dañado, se incluye igualmente")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should generate carátula for an existing cuaderno")
    void shouldGenerateCaratulaForExistingNotebook() throws Exception {
        List<Integer> ids = crearFolios(10, "Nuevo");
        MvcResult creado = mockMvc.perform(post("/api/v1/cuadernos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer idNotebook = mapper.readTree(creado.getResponse().getContentAsString()).get("idNotebook").asInt();

        MvcResult caratula = mockMvc.perform(get("/api/v1/cuadernos/" + idNotebook + "/caratula"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(caratula.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(caratula.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 404 for carátula of a missing cuaderno")
    void shouldReturnNotFoundForMissingNotebookCaratula() throws Exception {
        mockMvc.perform(get("/api/v1/cuadernos/999999/caratula"))
                .andExpect(status().isNotFound());
    }
}
