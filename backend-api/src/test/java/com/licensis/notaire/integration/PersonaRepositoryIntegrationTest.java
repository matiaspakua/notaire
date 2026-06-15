package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PersonaRepository Integration Tests")
class PersonaRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private Persona testPersona;
    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        testPersona = new Persona();
        testPersona.setNombre("Juan");
        testPersona.setApellido("Pérez");
        testPersona.setNumeroIdentificacion("12345678");
        testPersona.setEsCliente(false);
        testPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
    }

    @Test
    @DisplayName("Should persist and retrieve persona")
    void shouldPersistAndRetrievePersona() {
        Persona saved = personaRepository.save(testPersona);

        assertThat(saved.getIdPersona()).isNotNull();

        Optional<Persona> retrieved = personaRepository.findById(saved.getIdPersona());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getNombre()).isEqualTo("Juan");
                    assertThat(p.getApellido()).isEqualTo("Pérez");
                    assertThat(p.getNumeroIdentificacion()).isEqualTo("12345678");
                    assertThat(p.getEsCliente()).isFalse();
                });
    }

    @Test
    @DisplayName("Should find persona by numero identificacion")
    void shouldFindByNumeroIdentificacion() {
        personaRepository.save(testPersona);

        Optional<Persona> found = personaRepository.findByNumeroIdentificacion("12345678");

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getNombre()).isEqualTo("Juan"));
    }

    @Test
    @DisplayName("Should find personas by nombre containing")
    void shouldFindByNombreContaining() {
        Persona saved = personaRepository.save(testPersona);

        List<Persona> found = personaRepository.findByNombreContainingIgnoreCase("Juan");

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should find personas by apellido containing")
    void shouldFindByApellidoContaining() {
        Persona saved = personaRepository.save(testPersona);

        List<Persona> found = personaRepository.findByApellidoContainingIgnoreCase("Pérez");

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should find personas by tipo identificacion")
    void shouldFindByTipoIdentificacion() {
        personaRepository.save(testPersona);

        List<Persona> found = personaRepository
                .findByFkIdTipoIdentificacionIdTipoIdentificacion(tipoIdentificacion.getIdTipoIdentificacion());

        assertThat(found).hasSize(1)
                .allMatch(p -> p.getFkIdTipoIdentificacion().equals(tipoIdentificacion));
    }

    @Test
    @DisplayName("Should update persona")
    void shouldUpdatePersona() {
        Persona saved = personaRepository.save(testPersona);

        saved.setNombre("Carlos");
        saved.setApellido("Lopez");
        personaRepository.save(saved);

        Optional<Persona> updated = personaRepository.findById(saved.getIdPersona());

        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getNombre()).isEqualTo("Carlos");
                    assertThat(p.getApellido()).isEqualTo("Lopez");
                });
    }

    @Test
    @DisplayName("Should delete persona")
    void shouldDeletePersona() {
        Persona saved = personaRepository.save(testPersona);

        personaRepository.deleteById(saved.getIdPersona());

        Optional<Persona> deleted = personaRepository.findById(saved.getIdPersona());

        assertThat(deleted).isEmpty();
    }
}
