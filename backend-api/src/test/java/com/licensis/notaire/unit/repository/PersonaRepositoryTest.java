package com.licensis.notaire.unit.repository;

import static org.assertj.core.api.Assertions.*;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test-h2")
class PersonaRepositoryTest {

  @Autowired private PersonaRepository personaRepository;

  @BeforeEach
  void setUp() {
    personaRepository.deleteAll();
  }

  @Test
  @DisplayName("Should save and retrieve persona by ID")
  void shouldSaveAndRetrievePersona() {
    Persona persona = new Persona();
    persona.setNombre("Juan Pérez");

    Persona saved = personaRepository.save(persona);

    assertThat(saved.getIdPersona()).isNotNull();
    assertThat(saved.getNombre()).isEqualTo("Juan Pérez");

    Optional<Persona> retrieved = personaRepository.findById(saved.getIdPersona());
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get().getNombre()).isEqualTo("Juan Pérez");
  }

  @Test
  @DisplayName("Should find personas by name containing")
  void shouldFindPersonasByNameContaining() {
    personaRepository.save(createTestPersona("Juan García"));
    personaRepository.save(createTestPersona("Juan Pérez"));
    personaRepository.save(createTestPersona("María López"));

    List<Persona> results = personaRepository.findByNombreContainingIgnoreCase("Juan");

    assertThat(results).hasSize(2);
    assertThat(results).extracting(Persona::getNombre).contains("Juan García", "Juan Pérez");
  }

  @Test
  @DisplayName("Should find all personas")
  void shouldFindAllPersonas() {
    personaRepository.save(createTestPersona("Persona 1"));
    personaRepository.save(createTestPersona("Persona 2"));
    personaRepository.save(createTestPersona("Persona 3"));

    List<Persona> all = personaRepository.findAll();

    assertThat(all).hasSize(3);
  }

  @Test
  @DisplayName("Should update persona")
  void shouldUpdatePersona() {
    Persona persona = new Persona();
    persona.setNombre("Original Name");
    Persona saved = personaRepository.save(persona);

    saved.setNombre("Updated Name");
    Persona updated = personaRepository.save(saved);

    assertThat(updated.getNombre()).isEqualTo("Updated Name");
  }

  @Test
  @DisplayName("Should delete persona by ID")
  void shouldDeletePersonaById() {
    Persona persona = createTestPersona("To Delete");
    Persona saved = personaRepository.save(persona);
    assertThat(personaRepository.count()).isEqualTo(1);

    personaRepository.deleteById(saved.getIdPersona());

    assertThat(personaRepository.count()).isEqualTo(0);
    assertThat(personaRepository.findById(saved.getIdPersona())).isEmpty();
  }

  @Test
  @DisplayName("Should handle non-existent persona gracefully")
  void shouldReturnEmptyForNonExistentPersona() {
    Optional<Persona> result = personaRepository.findById(9999);
    assertThat(result).isEmpty();
  }

  private Persona createTestPersona(String nombre) {
    Persona persona = new Persona();
    persona.setNombre(nombre);
    return persona;
  }
}
