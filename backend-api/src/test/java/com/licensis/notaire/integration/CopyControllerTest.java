package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU87 - Vincular Escritura a Folio y Copia a Testimonio (issue #838): rechaza
 * la creación de una copia cuando el testimonio de origen ya tiene un
 * movimiento inscripto (RF asociado).
 */
@DisplayName("CopiaController#create — CU87 integration tests")
class CopyControllerTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeedRepository deedRepository;

    @Autowired
    private TestimonyRepository testimonyRepository;

    @Autowired
    private TestimonyMovementRepository testimonyMovementRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Testimony createTestimony() {
        Deed deed = new Deed();
        deed.setNumber((int) (System.currentTimeMillis() % 1_000_000));
        deed.setDateDeedrecording(new Date());
        deed.setStatus("Firmada");
        deed = deedRepository.save(deed);

        Testimony testimony = new Testimony();
        testimony.setNumber((int) (System.currentTimeMillis() % 1_000_000));
        testimony.setFkIdDeed(deed);
        return testimonyRepository.save(testimony);
    }

    private void addMovement(Testimony testimony, boolean registered) {
        TestimonyMovement movement = new TestimonyMovement();
        movement.setTestimony(testimony);
        movement.setDateEntry(new Date());
        movement.setRegistered(registered);
        movement.setCardNumber(1);
        testimonyMovementRepository.save(movement);
    }

    private String copyBody(int number, int idTestimony) {
        return """
                {
                  "number": %d,
                  "datePrinting": "2026-06-16",
                  "fkIdTestimony": {"idTestimony": %d}
                }
                """.formatted(number, idTestimony);
    }

    @Test
    @DisplayName("Should return 409 when testimony already has an registered movimiento")
    void shouldRejectCopyWhenTestimonyHasRegisteredMovement() throws Exception {
        Testimony testimony = createTestimony();
        addMovement(testimony, true);

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copyBody(8001, testimony.getIdTestimony())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should create copia when testimony has a non-registered movimiento")
    void shouldCreateCopyWhenTestimonyHasNoRegisteredMovement() throws Exception {
        Testimony testimony = createTestimony();
        addMovement(testimony, false);

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copyBody(8002, testimony.getIdTestimony())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should create copia when testimony has no movimientos")
    void shouldCreateCopyWhenTestimonyHasNoMovimientos() throws Exception {
        Testimony testimony = createTestimony();

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copyBody(8003, testimony.getIdTestimony())))
                .andExpect(status().isCreated());
    }
}
