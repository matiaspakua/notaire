package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
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
@DisplayName("CU39 — Plantilla de costos de documents por type de trámite (Issue #823)")
class DocumentCostTemplateControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private ProcedureType procedureType;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        procedureType = procedureTypeRepository.findById(1).orElseThrow();
    }

    private DocumentType crearDocumentType(String name) {
        DocumentType documentType = new DocumentType();
        documentType.setName(name);
        documentType.setExpires(false);
        documentType.setDeliveredBy("Cliente");
        documentType.setReturned(false);
        documentType.setEnabled(true);
        return documentTypeRepository.save(documentType);
    }

    private String crearCostBody(Integer idDocumentType, Float fixedAmount, Float variablePercentage) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("idProcedureType", procedureType.getIdProcedureType());
        body.put("idDocumentType", idDocumentType);
        body.put("fixedAmount", fixedAmount);
        body.put("variablePercentage", variablePercentage);
        return mapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("Should return costos by type de tramite")
    void shouldReturnCostosByTypeProcedure() throws Exception {
        DocumentType documentType = crearDocumentType("Escritura previa");

        mockMvc.perform(post("/api/v1/plantilla-costos-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearCostBody(documentType.getIdDocumentType(), 2000f, null)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/plantilla-costos-documento/tipo-tramite/" + procedureType.getIdProcedureType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fixedAmount").value(2000f));
    }

    @Test
    @DisplayName("Should return empty list when no costos defined")
    void shouldReturnEmptyListWhenNoCostosDefined() throws Exception {
        mockMvc.perform(get("/api/v1/plantilla-costos-documento/tipo-tramite/" + procedureType.getIdProcedureType()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
