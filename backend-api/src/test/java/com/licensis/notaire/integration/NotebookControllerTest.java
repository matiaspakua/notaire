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
@DisplayName("CU80 — Notebook of folios creation and cover")
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

    private List<Integer> createFolios(int cantidad, String status) {
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
    @DisplayName("Should create notebook from ten consecutive folios")
    void shouldCreateNotebookFromConsecutiveFolios() throws Exception {
        List<Integer> ids = createFolios(10, "New");

        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.number").exists());
    }

    @Test
    @DisplayName("Should reject notebook when folio count is not a multiple of ten")
    void shouldRejectNotebookWhenFolioCountNotMultipleOfTen() throws Exception {
        List<Integer> ids = createFolios(5, "New");

        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject notebook with non-consecutive folios")
    void shouldRejectNotebookWithNonConsecutiveFolios() throws Exception {
        List<Integer> ids = createFolios(11, "New");
        List<Integer> discontinuos = new ArrayList<>(ids.subList(0, 9));
        discontinuos.add(ids.get(10));

        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(discontinuos, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject notebook with a folio already assigned")
    void shouldRejectNotebookWithFolioAlreadyAssigned() throws Exception {
        List<Integer> primerLote = createFolios(10, "New");
        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(primerLote, null)))
                .andExpect(status().isCreated());

        List<Integer> segundoLote = createFolios(10, "New");
        List<Integer> conFolioReasignado = new ArrayList<>(segundoLote.subList(0, 9));
        conFolioReasignado.add(primerLote.get(0));

        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(conFolioReasignado, null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create notebook with a justified damaged folio")
    void shouldCreateNotebookWithJustifiedDamagedFolio() throws Exception {
        List<Integer> ids = createFolios(9, "New");
        ids.addAll(createFolios(1, "Errose"));

        mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, "Folio 10 is damaged and is included anyway")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should generate cover for an existing notebook")
    void shouldGenerateCoverForExistingNotebook() throws Exception {
        List<Integer> ids = createFolios(10, "New");
        MvcResult created = mockMvc.perform(post("/api/v1/notebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createNotebookBody(ids, null)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer idNotebook = mapper.readTree(created.getResponse().getContentAsString()).get("idNotebook").asInt();

        MvcResult cover = mockMvc.perform(get("/api/v1/notebooks/" + idNotebook + "/cover"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(cover.getResponse().getContentType()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(cover.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 404 for cover of a missing notebook")
    void shouldReturnNotFoundForMissingNotebookCover() throws Exception {
        mockMvc.perform(get("/api/v1/notebooks/999999/cover"))
                .andExpect(status().isNotFound());
    }
}
