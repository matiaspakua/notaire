package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import com.licensis.notaire.repository.TestimonioRepository;
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
class CopiaControllerTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EscrituraRepository escrituraRepository;

    @Autowired
    private TestimonioRepository testimonioRepository;

    @Autowired
    private MovimientoTestimonioRepository movimientoTestimonioRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Testimonio createTestimonio() {
        Escritura escritura = new Escritura();
        escritura.setNumero((int) (System.currentTimeMillis() % 1_000_000));
        escritura.setFechaEscrituracion(new Date());
        escritura.setEstado("Firmada");
        escritura = escrituraRepository.save(escritura);

        Testimonio testimonio = new Testimonio();
        testimonio.setNumero((int) (System.currentTimeMillis() % 1_000_000));
        testimonio.setFkIdEscritura(escritura);
        return testimonioRepository.save(testimonio);
    }

    private void addMovimiento(Testimonio testimonio, boolean inscripta) {
        MovimientoTestimonio movimiento = new MovimientoTestimonio();
        movimiento.setTestimonio(testimonio);
        movimiento.setFechaIngreso(new Date());
        movimiento.setInscripta(inscripta);
        movimiento.setNumeroCarton(1);
        movimientoTestimonioRepository.save(movimiento);
    }

    private String copiaBody(int numero, int idTestimonio) {
        return """
                {
                  "numero": %d,
                  "fechaImpresion": "2026-06-16",
                  "fkIdTestimonio": {"idTestimonio": %d}
                }
                """.formatted(numero, idTestimonio);
    }

    @Test
    @DisplayName("Should return 409 when testimonio already has an inscripta movimiento")
    void shouldRejectCopiaWhenTestimonioHasInscriptaMovimiento() throws Exception {
        Testimonio testimonio = createTestimonio();
        addMovimiento(testimonio, true);

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copiaBody(8001, testimonio.getIdTestimonio())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should create copia when testimonio has a non-inscripta movimiento")
    void shouldCreateCopiaWhenTestimonioHasNoInscriptaMovimiento() throws Exception {
        Testimonio testimonio = createTestimonio();
        addMovimiento(testimonio, false);

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copiaBody(8002, testimonio.getIdTestimonio())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should create copia when testimonio has no movimientos")
    void shouldCreateCopiaWhenTestimonioHasNoMovimientos() throws Exception {
        Testimonio testimonio = createTestimonio();

        mockMvc.perform(post("/api/v1/copia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(copiaBody(8003, testimonio.getIdTestimonio())))
                .andExpect(status().isCreated());
    }
}
