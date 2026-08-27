package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.HistorialRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GET /api/v1/gestiones/{id}/historial.
 * Seeds Historial entries out of chronological order and asserts the
 * endpoint returns them sorted by date, per CU13.
 */
@RequirementCoverage({"CU13"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("GET /gestiones/{id}/historial — returns the ordered bitácora (CU13)")
class GestionBitacoraControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private EstadoDeGestionRepository estadoRepository;
    @Autowired
    private GestionDeEscrituraRepository gestionRepository;
    @Autowired
    private HistorialRepository historialRepository;
    @Autowired
    private PersonaRepository personaRepository;

    private MockMvc mockMvc;
    private Integer gestionConHistorialId;
    private Integer gestionSinHistorialId;
    private EstadoDeGestion estadoInicial;
    private EstadoDeGestion estadoFinal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedGestionesConHistorial();
    }

    private EstadoDeGestion estado(String nombre) {
        EstadoDeGestion estado = new EstadoDeGestion();
        estado.setNombre(nombre + " " + System.nanoTime());
        return estadoRepository.save(estado);
    }

    private Integer createGestion(EstadoDeGestion estado) {
        GestionDeEscritura gestion = new GestionDeEscritura();
        gestion.setNumero((int) (System.nanoTime() % 100000));
        gestion.setEncabezado("Gestion Bitacora IT");
        gestion.setFechaInicio(new Date());
        gestion.setFkIdEstadoDeGestion(estado);
        gestion.setFkIdPersonaEscribano(personaRepository.findAll().get(0));
        return gestionRepository.save(gestion).getIdGestion();
    }

    private void historial(Integer idGestion, EstadoDeGestion estado, long epochMillis) {
        Historial entry = new Historial();
        entry.setFkIdGestion(gestionRepository.findById(idGestion).orElseThrow());
        entry.setFkIdEstadoGestion(estado);
        entry.setFecha(new Date(epochMillis));
        historialRepository.save(entry);
    }

    private void seedGestionesConHistorial() {
        estadoInicial = estado("Bitacora Inicial");
        estadoFinal = estado("Bitacora Final");

        gestionConHistorialId = createGestion(estadoFinal);
        long oneDayMillis = 86_400_000L;
        historial(gestionConHistorialId, estadoFinal, 3 * oneDayMillis);
        historial(gestionConHistorialId, estadoInicial, oneDayMillis);

        gestionSinHistorialId = createGestion(estadoInicial);
    }

    @Test
    @DisplayName("Consulta devuelve el historial completo ordenado")
    void shouldReturnOrderedHistorial() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/historial", gestionConHistorialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].estadoGestionNombre").value(estadoInicial.getNombre()))
                .andExpect(jsonPath("$[1].estadoGestionNombre").value(estadoFinal.getNombre()));
    }

    @Test
    @DisplayName("Should return an empty list when the gestión has no historial entries")
    void shouldReturnEmptyListWhenNoHistorialEntries() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/historial", gestionSinHistorialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist")
    void shouldReturn404WhenGestionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/historial"))
                .andExpect(status().isNotFound());
    }
}
