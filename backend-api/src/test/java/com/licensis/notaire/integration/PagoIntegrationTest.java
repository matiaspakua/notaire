package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

@DisplayName("Pago REST integration tests — CU15 metodoPago persistence")
class PagoIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private Integer idPresupuesto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        Persona persona = new Persona();
        persona.setNombre("Cliente");
        persona.setApellido("Test");
        persona.setNumeroIdentificacion("87654321");
        persona.setEsCliente(true);
        persona.setFkIdTipoIdentificacion(tipoIdentificacion);
        persona = personaRepository.save(persona);

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumero((int) (System.currentTimeMillis() % 10000));
        presupuesto.setFecha(new Date());
        presupuesto.setEncabezado("Presupuesto Test");
        presupuesto.setEstado("PENDIENTE");
        presupuesto.setMontoInmueble(500000f);
        presupuesto.setFkIdPersona(persona);
        presupuesto = presupuestoRepository.save(presupuesto);
        idPresupuesto = presupuesto.getIdPresupuesto();
    }

    @Test
    @DisplayName("Should return the same metodoPago when processing a payment")
    void shouldReturnMetodoPagoOnCreate() throws Exception {
        String json = """
                {
                    "idPresupuesto": %d,
                    "monto": 100000.0,
                    "fecha": "2026-08-20",
                    "observaciones": "Primer pago",
                    "metodoPago": "Efectivo"
                }
                """.formatted(idPresupuesto);

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metodoPago").value("Efectivo"));
    }

    @Test
    @DisplayName("Should reflect the stored metodoPago when retrieving a payment by ID")
    void shouldReturnStoredMetodoPagoOnGetById() throws Exception {
        String json = """
                {
                    "idPresupuesto": %d,
                    "monto": 100000.0,
                    "fecha": "2026-08-20",
                    "observaciones": "Primer pago",
                    "metodoPago": "Transferencia"
                }
                """.formatted(idPresupuesto);

        MvcResult created = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        Integer idPago = mapper.readTree(created.getResponse().getContentAsString())
                .get("idPago").asInt();

        mockMvc.perform(get("/api/v1/pagos/" + idPago))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoPago").value("Transferencia"));
    }

    @Test
    @DisplayName("Should persist an updated metodoPago after editing a payment")
    void shouldPersistUpdatedMetodoPago() throws Exception {
        String createJson = """
                {
                    "idPresupuesto": %d,
                    "monto": 100000.0,
                    "fecha": "2026-08-20",
                    "observaciones": "Primer pago",
                    "metodoPago": "Efectivo"
                }
                """.formatted(idPresupuesto);

        MvcResult created = mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Integer idPago = mapper.readTree(created.getResponse().getContentAsString())
                .get("idPago").asInt();

        String updateJson = """
                {
                    "monto": 120000.0,
                    "fecha": "2026-08-20",
                    "observaciones": "Editado",
                    "metodoPago": "Cheque"
                }
                """;

        mockMvc.perform(put("/api/v1/pagos/" + idPago)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoPago").value("Cheque"));

        mockMvc.perform(get("/api/v1/pagos/" + idPago))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metodoPago").value("Cheque"));
    }
}
