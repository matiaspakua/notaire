package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TramiteRepository;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@code Tramite} has two bidirectional EAGER/LAZY relationships that Jackson recurses through
 * unless the back-reference side is excluded:
 * <ul>
 *   <li>{@code Tramite.presupuestoList} (EAGER) &lt;-&gt; {@code Presupuesto.fkIdTramite} (EAGER)</li>
 *   <li>{@code Tramite.fkIdGestion} (EAGER) &lt;-&gt; {@code GestionDeEscritura.tramiteList} (LAZY)</li>
 * </ul>
 * Both previously 500'd GET /api/v1/tramites ("Failed to write request") as soon as production
 * data populated either relation — which V1 seed data does via {@code fk_id_gestion}.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Tramite — list endpoint serializes without cyclic recursion")
class TramiteSerializationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private TramiteRepository tramiteRepository;
    @Autowired
    private GestionDeEscrituraRepository gestionRepository;
    @Autowired
    private EstadoDeGestionRepository estadoRepository;
    @Autowired
    private PersonaRepository personaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createTipoTramite() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/tipo-tramite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Tramite Ciclo Presupuesto", "seInscribe": false,
                                 "seArchiva": false, "asociaInmuebles": false}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTipoTramite").asInt();
    }

    private Integer createTramite(Integer tipoTramiteId) throws Exception {
        String body = """
                {"observaciones": "tramite ciclo IT", "fkIdTipoTramite": {"idTipoTramite": %d}}
                """.formatted(tipoTramiteId);
        MvcResult result = mockMvc.perform(post("/api/v1/tramites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTramite").asInt();
    }

    private void createPresupuestoForTramite(Integer tramiteId) throws Exception {
        String body = """
                {"numero": 1, "encabezado": "presupuesto ciclo IT", "estado": "PENDIENTE",
                 "fecha": "2026-07-24", "fkIdTramite": {"idTramite": %d}}
                """.formatted(tramiteId);
        mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should list tramites as valid JSON when a presupuesto references the tramite")
    void shouldListTramitesWithoutCyclicRecursion() throws Exception {
        Integer tipoTramiteId = createTipoTramite();
        Integer tramiteId = createTramite(tipoTramiteId);
        createPresupuestoForTramite(tramiteId);

        MvcResult result = mockMvc.perform(get("/api/v1/tramites"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = body.get("content");
        assertThat(content).as("Page response should have a content array").isNotNull();

        JsonNode tramite = null;
        for (JsonNode candidate : content) {
            if (candidate.get("idTramite").asInt() == tramiteId) {
                tramite = candidate;
                break;
            }
        }
        assertThat(tramite).as("created tramite should be in the list").isNotNull();

        JsonNode presupuestoList = tramite.get("presupuestoList");
        assertThat(presupuestoList).as("tramite should embed its presupuestoList").isNotNull();
        assertThat(presupuestoList).isNotEmpty();
        assertThat(presupuestoList.get(0).has("fkIdTramite"))
                .as("presupuesto must not embed its fkIdTramite (cyclic reference)")
                .isFalse();
    }

    @Test
    @DisplayName("Should list tramites as valid JSON when a gestion references the tramite (matches V1 seed data)")
    void shouldListTramitesWithoutCyclicRecursionThroughGestion() throws Exception {
        Integer tipoTramiteId = createTipoTramite();
        TipoDeTramite tipo = new TipoDeTramite();
        tipo.setIdTipoTramite(tipoTramiteId);

        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre("Ciclo IT Estado");
        estado = estadoRepository.save(estado);

        Persona escribano = personaRepository.findAll().get(0);

        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setIdGestion(null);
        gestion.setNumero(123456);
        gestion.setEncabezado("Gestion Ciclo IT");
        gestion.setFechaInicio(new Date());
        gestion.setFkIdEstadoDeGestion(estado);
        gestion.setFkIdPersonaEscribano(escribano);
        gestion = gestionRepository.save(gestion);

        Tramite tramite = new Tramite();
        tramite.setIdTramite(null);
        tramite.setFkIdGestion(gestion);
        tramite.setFkIdTipoTramite(tipo);
        tramiteRepository.save(tramite);

        MvcResult result = mockMvc.perform(get("/api/v1/tramites"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body2 = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = body2.get("content");
        assertThat(content).as("Page response should have a content array").isNotNull();

        JsonNode found = null;
        for (JsonNode candidate : content) {
            JsonNode gestionNode = candidate.get("fkIdGestion");
            if (gestionNode != null && !gestionNode.isNull()
                    && gestionNode.get("idGestion").asInt() == gestion.getIdGestion()) {
                found = candidate;
                break;
            }
        }
        assertThat(found).as("tramite linked to the gestion should be in the list").isNotNull();
        assertThat(found.get("fkIdGestion").has("tramiteList"))
                .as("gestion must not embed its tramiteList (cyclic reference)")
                .isFalse();
    }
}
