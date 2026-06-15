package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.service.PersonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PersonaService Integration Tests")
class PersonaServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PersonaService personaService;

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
    @DisplayName("Should persist persona through service")
    void shouldPersistPersonaThroughService() {
        Persona saved = personaService.save(testPersona);

        assertThat(saved.getIdPersona()).isNotNull();
        assertThat(personaRepository.findById(saved.getIdPersona())).isPresent();
    }

    @Test
    @DisplayName("Should find persona by id through service")
    void shouldFindPersonaByIdThroughService() {
        Persona saved = personaService.save(testPersona);

        Optional<Persona> found = personaService.findById(saved.getIdPersona());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getNombre()).isEqualTo("Juan"));
    }

    @Test
    @DisplayName("Should find all personas through service")
    void shouldFindAllPersonasThroughService() {
        personaService.save(testPersona);

        List<Persona> all = personaService.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getNombre().equals("Juan"));
    }

    @Test
    @DisplayName("Should delete persona through service")
    void shouldDeletePersonaThroughService() {
        Persona saved = personaService.save(testPersona);

        personaService.deleteById(saved.getIdPersona());

        Optional<Persona> deleted = personaRepository.findById(saved.getIdPersona());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should search personas with all filters")
    void shouldSearchPersonasWithAllFilters() {
        Persona saved = personaService.save(testPersona);

        List<Persona> found = personaService.buscar(
                "Juan",
                "Pérez",
                "12345678",
                tipoIdentificacion.getIdTipoIdentificacion(),
                false
        );

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should search personas with nombre filter only")
    void shouldSearchPersonasWithNombreOnly() {
        Persona saved = personaService.save(testPersona);

        List<Persona> found = personaService.buscar("Juan", null, null, null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should search personas with esCliente filter")
    void shouldSearchPersonasWithEsClienteFilter() {
        Persona cliente = new Persona();
        cliente.setNombre("Carlos");
        cliente.setApellido("Lopez");
        cliente.setNumeroIdentificacion("87654321");
        cliente.setEsCliente(true);
        cliente.setFkIdTipoIdentificacion(tipoIdentificacion);
        Persona saved = personaService.save(cliente);

        List<Persona> found = personaService.buscar(null, null, null, null, true);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona())
                        && p.getEsCliente() == true);
    }

    @Test
    @DisplayName("Should update persona through service")
    void shouldUpdatePersonaThroughService() {
        Persona saved = personaService.save(testPersona);

        saved.setNombre("Pedro");
        saved.setApellido("Garcia");
        personaService.save(saved);

        Optional<Persona> updated = personaRepository.findById(saved.getIdPersona());
        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getNombre()).isEqualTo("Pedro");
                    assertThat(p.getApellido()).isEqualTo("Garcia");
                });
    }

    @Test
    @DisplayName("Should handle empty search results")
    void shouldHandleEmptySearchResults() {
        List<Persona> found = personaService.buscar(
                "NonexistentName",
                null,
                null,
                null,
                null
        );

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should maintain transaction consistency")
    void shouldMaintainTransactionConsistency() {
        Persona saved = personaService.save(testPersona);
        Integer savedId = saved.getIdPersona();

        personaService.deleteById(savedId);
        Optional<Persona> found = personaService.findById(savedId);

        assertThat(found).isEmpty();
    }
}
