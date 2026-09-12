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
@DisplayName("CU81 — Gestión de trámites en Protocolo Auxiliar")
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

    private FolioType crearTypeAuxiliary() {
        FolioType type = new FolioType();
        type.setName("Protocolo Auxiliar " + System.nanoTime());
        type.setIsAuxiliary(true);
        type.setEnabled(true);
        return folioTypeRepository.save(type);
    }

    private Folio crearFolioAuxiliary(FolioType type, int number) {
        Folio folio = new Folio();
        folio.setNumber(number);
        folio.setYear(2026);
        folio.setStatus("Nuevo");
        folio.setFkIdFolioType(type);
        folio.setFkIdNotaryPerson(notary);
        return folioRepository.save(folio);
    }

    @Test
    @DisplayName("Should list available auxiliar folios")
    void shouldListAvailableFoliosAuxiliares() throws Exception {
        FolioType type = crearTypeAuxiliary();
        crearFolioAuxiliary(type, 501);

        mockMvc.perform(get("/api/v1/protocolo-auxiliar/folios-disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.number == 501)]").exists());
    }

    @Test
    @DisplayName("Should return an empty list when there are no auxiliar folios available")
    void shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/protocolo-auxiliar/folios-disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(org.hamcrest.Matchers.hasEntry("number", 999999)))));
    }

    @Test
    @DisplayName("Should create deed on an available auxiliar folio with its own correlative number")
    void shouldCreateDeedOnAvailableFolioAuxiliary() throws Exception {
        FolioType type = crearTypeAuxiliary();
        Folio folio = crearFolioAuxiliary(type, 601);

        Map<String, Object> body = new HashMap<>();
        body.put("idFolio", folio.getIdFolio());
        body.put("body", "Acta de certificación de firma");

        mockMvc.perform(post("/api/v1/protocolo-auxiliar/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").exists());
    }

    @Test
    @DisplayName("Should reject starting an deed when no auxiliar folio is available")
    void shouldRejectDeedWhenNoFolioAuxiliaryAvailable() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("body", "Acta de certificación de firma");

        mockMvc.perform(post("/api/v1/protocolo-auxiliar/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
