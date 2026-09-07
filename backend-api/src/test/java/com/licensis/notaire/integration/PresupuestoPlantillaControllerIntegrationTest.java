package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.ConceptoRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.testing.RequirementCoverage;
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
import org.springframework.web.context.WebApplicationContext;

@RequirementCoverage({"CU39"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Presupuesto — cargar ítems desde la plantilla del tipo de trámite (CU39)")
class PresupuestoPlantillaControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Autowired
    private ConceptoRepository conceptoRepository;

    @Autowired
    private PlantillaPresupuestoRepository plantillaPresupuestoRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona() throws Exception {
        String body = """
                {"nombre": "Cliente", "apellido": "Plantilla IT", "numeroIdentificacion": "%s",
                 "esCliente": true, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted("60" + (System.nanoTime() % 1000000));
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createPresupuesto(Integer clienteId) throws Exception {
        String body = """
                {"numero": %d, "fecha": "2026-01-01", "encabezado": "Presupuesto Plantilla IT",
                 "estado": "PENDIENTE", "monto": 1000.00, "persona": {"idPersona": %d}}
                """.formatted((int) (System.nanoTime() % 100000), clienteId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPresupuesto").asInt();
    }

    private TipoDeTramite createTipoDeTramiteConPlantilla(String nombreConcepto, float valor, int porcentaje) {
        TipoDeTramite tipoDeTramite = new TipoDeTramite();
        tipoDeTramite.setNombre("Tipo Tramite Plantilla IT " + System.nanoTime());
        tipoDeTramiteRepository.save(tipoDeTramite);

        Concepto concepto = new Concepto();
        concepto.setNombre(nombreConcepto);
        concepto.setValor(valor);
        concepto.setPorcentaje(porcentaje);
        conceptoRepository.save(concepto);

        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setPlantillaPresupuestoPK(
                new PlantillaPresupuestoPK(tipoDeTramite.getIdTipoTramite(), concepto.getIdConcepto()));
        plantilla.setTipoDeTramite(tipoDeTramite);
        plantilla.setConcepto(concepto);
        plantillaPresupuestoRepository.save(plantilla);

        return tipoDeTramite;
    }

    @Test
    @DisplayName("Should load presupuesto items from the tipo de trámite's plantilla")
    void shouldLoadItemsFromPlantilla() throws Exception {
        Integer clienteId = createPersona();
        Integer presupuestoId = createPresupuesto(clienteId);
        TipoDeTramite tipoDeTramite = createTipoDeTramiteConPlantilla("Honorarios IT", 1500f, 10);

        mockMvc.perform(post("/api/v1/presupuestos/" + presupuestoId + "/items-desde-plantilla")
                        .param("tipoTramiteId", tipoDeTramite.getIdTipoTramite().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Honorarios IT"))
                .andExpect(jsonPath("$[0].valor").value(1500.0));
    }

    @Test
    @DisplayName("Should return 400 when the tipo de trámite has no plantilla configured")
    void shouldRejectWhenNoPlantillaConfigured() throws Exception {
        Integer clienteId = createPersona();
        Integer presupuestoId = createPresupuesto(clienteId);

        TipoDeTramite tipoSinPlantilla = new TipoDeTramite();
        tipoSinPlantilla.setNombre("Tipo Sin Plantilla IT " + System.nanoTime());
        tipoDeTramiteRepository.save(tipoSinPlantilla);

        mockMvc.perform(post("/api/v1/presupuestos/" + presupuestoId + "/items-desde-plantilla")
                        .param("tipoTramiteId", tipoSinPlantilla.getIdTipoTramite().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when the presupuesto does not exist")
    void shouldReturnNotFoundForUnknownPresupuesto() throws Exception {
        TipoDeTramite tipoDeTramite = createTipoDeTramiteConPlantilla("Sellado IT", 500f, 0);

        mockMvc.perform(post("/api/v1/presupuestos/999999/items-desde-plantilla")
                        .param("tipoTramiteId", tipoDeTramite.getIdTipoTramite().toString()))
                .andExpect(status().isNotFound());
    }
}
