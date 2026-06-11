package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.PersonaRepository;
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

@SpringBootTest
@ActiveProfiles("test-h2")
class PersonaControllerIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private PersonaRepository personaRepository;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    objectMapper = new ObjectMapper();
    personaRepository.deleteAll();
  }

  @Test
  @DisplayName("Should return empty list of personas initially")
  void shouldReturnEmptyListInitially() throws Exception {
    mockMvc
        .perform(get("/api/v1/personas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("Should create new persona successfully")
  void shouldCreateNewPersona() throws Exception {
    Persona persona = new Persona();
    persona.setNombre("Juan Pérez");

    mockMvc
        .perform(
            post("/api/v1/personas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(persona)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nombre").value("Juan Pérez"));

    assertThat(personaRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Should retrieve existing persona by ID")
  void shouldReturnExistingPersona() throws Exception {
    Persona persona = new Persona();
    persona.setNombre("María García");
    Persona saved = personaRepository.save(persona);

    mockMvc
        .perform(get("/api/v1/personas/{id}", saved.getIdPersona()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("María García"))
        .andExpect(jsonPath("$.idPersona").value(saved.getIdPersona()));
  }

  @Test
  @DisplayName("Should update persona data successfully")
  void shouldUpdatePersona() throws Exception {
    Persona persona = new Persona();
    persona.setNombre("Pedro López");
    Persona saved = personaRepository.save(persona);

    Persona update = new Persona();
    update.setNombre("Pedro López Actualizado");

    mockMvc
        .perform(
            put("/api/v1/personas/{id}", saved.getIdPersona())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Pedro López Actualizado"))
        .andExpect(jsonPath("$.idPersona").value(saved.getIdPersona()));
  }

  @Test
  @DisplayName("Should delete persona successfully")
  void shouldDeletePersona() throws Exception {
    Persona persona = new Persona();
    persona.setNombre("Ana Martínez");
    Persona saved = personaRepository.save(persona);
    assertThat(personaRepository.count()).isEqualTo(1);

    mockMvc
        .perform(delete("/api/v1/personas/{id}", saved.getIdPersona()))
        .andExpect(status().isNoContent());

    assertThat(personaRepository.findById(saved.getIdPersona())).isEmpty();
    assertThat(personaRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should return all created personas")
  void shouldReturnAllPersonas() throws Exception {
    createTestPersona("Juan");
    createTestPersona("María");
    createTestPersona("Carlos");

    mockMvc
        .perform(get("/api/v1/personas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }

  @Test
  @DisplayName("Should return 404 when persona not found")
  void shouldReturn404WhenPersonaNotFound() throws Exception {
    mockMvc
        .perform(get("/api/v1/personas/{id}", 9999))
        .andExpect(status().isNotFound());
  }

  private Persona createTestPersona(String nombre) {
    Persona persona = new Persona();
    persona.setNombre(nombre);
    return personaRepository.save(persona);
  }
}
