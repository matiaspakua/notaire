package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PersonaRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("CU28 — EstadoDeGestion referential integrity and search (issue #437)")
class EstadoDeGestionReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GestionDeEscrituraRepository gestionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private EstadoDeGestionRepository estadoRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createEstado(String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/estado-gestion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "%s", "observaciones": "Test"}
                                """.formatted(nombre)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idEstadoGestion").asInt();
    }

    private void linkEstadoToGestion(int idEstadoGestion) {
        Persona escribano = personaRepository.findById(1).orElseThrow();
        EstadoDeGestion estado = estadoRepository.findById(idEstadoGestion).orElseThrow();

        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setFechaInicio(new Date());
        gestion.setNumero(90000 + idEstadoGestion);
        gestion.setEncabezado("Test gestion for estado " + idEstadoGestion);
        gestion.setFkIdPersonaEscribano(escribano);
        gestion.setFkIdEstadoDeGestion(estado);
        gestionRepository.save(gestion);
    }

    @Test
    @DisplayName("Should return inUse=false when no gestion references the estado")
    void shouldReturnInUseFalseWhenEstadoIsNotReferenced() throws Exception {
        int id = createEstado("EstadoFree_InUse");

        mockMvc.perform(get("/api/v1/estado-gestion/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when a gestion references the estado")
    void shouldReturnInUseTrueWhenEstadoIsReferenced() throws Exception {
        int id = createEstado("EstadoUsed_InUse");
        linkEstadoToGestion(id);

        mockMvc.perform(get("/api/v1/estado-gestion/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing an estado that is in use")
    void shouldReturn409WhenEditingEstadoInUse() throws Exception {
        int id = createEstado("EstadoEditBlocked");
        linkEstadoToGestion(id);

        mockMvc.perform(put("/api/v1/estado-gestion/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "EstadoEditBlocked_Updated", "observaciones": "Updated"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing an estado that is NOT in use")
    void shouldAllowEditingEstadoNotInUse() throws Exception {
        int id = createEstado("EstadoEditAllowed");

        mockMvc.perform(put("/api/v1/estado-gestion/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "EstadoEditAllowed_Updated", "observaciones": "Updated"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting an estado that is in use")
    void shouldReturn409WhenDeletingEstadoInUse() throws Exception {
        int id = createEstado("EstadoDeleteBlocked");
        linkEstadoToGestion(id);

        mockMvc.perform(delete("/api/v1/estado-gestion/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search estados by nombre")
    void shouldSearchEstadosByNombre() throws Exception {
        createEstado("BuscarEsteEstado");

        mockMvc.perform(get("/api/v1/estado-gestion/search").param("nombre", "BuscarEste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("BuscarEsteEstado"));
    }
}
