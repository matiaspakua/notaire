package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("CU81 — Management of procedures in Auxiliary protocol")
class AuxiliaryProtocolControllerTest {

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        notary = personRepository.findById(1).orElseThrow();
    }

    private FolioType createAuxiliaryType() {
        FolioType type = new FolioType();
        type.setName("Protocolo Auxiliar " + System.nanoTime());
        type.setIsAuxiliary(true);
        type.setEnabled(true);
        return folioTypeRepository.save(type);
    }

    private Folio createAuxiliaryFolio(FolioType type, int number) {
        Folio folio = new Folio();
        folio.setNumber(number);
        folio.setYear(2026);
        folio.setStatus("New");
        folio.setFkIdFolioType(type);
        folio.setFkIdNotaryPerson(notary);
        return folioRepository.save(folio);
    }

    @Test
    @DisplayName("Should list available auxiliary folios")
    void shouldListAvailableAuxiliaryFolios() throws Exception {
        FolioType type = createAuxiliaryType();
        createAuxiliaryFolio(type, 501);

        mockMvc.perform(get("/api/v1/auxiliary-protocol/available-folios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == 501)]").exists());
    }

    @Test
    @DisplayName("Should return an empty list when there are no auxiliary folios available")
    void shouldReturnEmptyWhenNoAuxiliaryFoliosAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/auxiliary-protocol/available-folios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasEntry("number", 999999)))));
    }

    @Test
    @DisplayName("Should create deed on an available auxiliary folio with its own correlative number")
    void shouldCreateDeedOnAvailableAuxiliaryFolio() throws Exception {
        FolioType type = createAuxiliaryType();
        Folio folio = createAuxiliaryFolio(type, 601);

        Map<String, Object> body = new HashMap<>();
        body.put("idFolio", folio.getIdFolio());
        body.put("body", "Record of signature certification");

        mockMvc.perform(post("/api/v1/auxiliary-protocol/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").exists());
    }

    @Test
    @DisplayName("Should reject starting a deed when no auxiliary folio is available")
    void shouldRejectDeedWhenNoAuxiliaryFolioAvailable() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("body", "Record of signature certification");

        mockMvc.perform(post("/api/v1/auxiliary-protocol/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
