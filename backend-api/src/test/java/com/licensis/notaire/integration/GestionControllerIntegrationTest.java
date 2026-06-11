package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Gestion;
import com.licensis.notaire.repository.GestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test-h2")
class GestionControllerIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private GestionRepository gestionRepository;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    objectMapper = new ObjectMapper();
    gestionRepository.deleteAll();
  }

  @Test
  @DisplayName("Should return empty gestiones list initially")
  void shouldReturnEmptyGestionesListInitially() throws Exception {
    mockMvc
        .perform(get("/api/v1/gestiones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("Should create new gestion successfully")
  void shouldCreateNewGestion() throws Exception {
    Gestion gestion = new Gestion();
    gestion.setNombre("Escritura de Venta - Casa");
    gestion.setDescripcion("Venta de propiedad residencial");

    mockMvc
        .perform(
            post("/api/v1/gestiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gestion)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nombre").value("Escritura de Venta - Casa"))
        .andExpect(jsonPath("$.descripcion").value("Venta de propiedad residencial"));

    assertThat(gestionRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should retrieve gestion by ID")
  void shouldRetrieveGestionById() throws Exception {
    Gestion gestion = new Gestion();
    gestion.setNombre("Poder Notarial");
    gestion.setDescripcion("Otorgamiento de poder");
    Gestion saved = gestionRepository.save(gestion);

    mockMvc
        .perform(get("/api/v1/gestiones/{id}", saved.getIdGestion()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Poder Notarial"));
  }

  @Test
  @DisplayName("Should update gestion successfully")
  void shouldUpdateGestion() throws Exception {
    Gestion gestion = new Gestion();
    gestion.setNombre("Testamento");
    gestion.setDescripcion("Otorgamiento de testamento");
    Gestion saved = gestionRepository.save(gestion);

    Gestion update = new Gestion();
    update.setNombre("Testamento Actualizado");
    update.setDescripcion("Testamento con nuevas disposiciones");

    mockMvc
        .perform(
            put("/api/v1/gestiones/{id}", saved.getIdGestion())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Testamento Actualizado"));
  }

  @Test
  @DisplayName("Should delete gestion successfully")
  void shouldDeleteGestion() throws Exception {
    Gestion gestion = new Gestion();
    gestion.setNombre("Donación");
    gestion.setDescripcion("Donación de bien");
    Gestion saved = gestionRepository.save(gestion);

    mockMvc
        .perform(delete("/api/v1/gestiones/{id}", saved.getIdGestion()))
        .andExpect(status().isNoContent());

    assertThat(gestionRepository.findById(saved.getIdGestion())).isEmpty();
  }

  @Test
  @DisplayName("Should return multiple gestiones")
  void shouldReturnMultipleGestiones() throws Exception {
    gestionRepository.save(createTestGestion("Gestion 1"));
    gestionRepository.save(createTestGestion("Gestion 2"));
    gestionRepository.save(createTestGestion("Gestion 3"));

    mockMvc
        .perform(get("/api/v1/gestiones"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }

  @Test
  @DisplayName("Should return 404 for non-existent gestion")
  void shouldReturn404ForNonExistentGestion() throws Exception {
    mockMvc
        .perform(get("/api/v1/gestiones/{id}", 9999))
        .andExpect(status().isNotFound());
  }

  private Gestion createTestGestion(String nombre) {
    Gestion gestion = new Gestion();
    gestion.setNombre(nombre);
    gestion.setDescripcion("Descripción de " + nombre);
    return gestion;
  }
}
