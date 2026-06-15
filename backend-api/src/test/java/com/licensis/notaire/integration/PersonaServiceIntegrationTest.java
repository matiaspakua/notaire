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

    @Test
    @DisplayName("Should search personas with apellido filter only")
    void shouldSearchPersonasWithApellidoOnly() {
        Persona saved = personaService.save(testPersona);

        List<Persona> found = personaService.buscar(null, "Pérez", null, null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should search personas with numero_identificacion filter only")
    void shouldSearchPersonasWithNumeroIdentificacionOnly() {
        Persona saved = personaService.save(testPersona);

        List<Persona> found = personaService.buscar(null, null, "12345678", null, null);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should search personas with tipo_identificacion filter only")
    void shouldSearchPersonasWithTipoIdentificacionOnly() {
        Persona saved = personaService.save(testPersona);

        List<Persona> found = personaService.buscar(
                null,
                null,
                null,
                tipoIdentificacion.getIdTipoIdentificacion(),
                null
        );

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona()));
    }

    @Test
    @DisplayName("Should return empty list when searching with no matches")
    void shouldReturnEmptyListWhenSearchingWithNoMatches() {
        List<Persona> found = personaService.buscar(
                "NonexistentName",
                "NonexistentApellido",
                "99999999",
                null,
                null
        );

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should search personas with combination of filters")
    void shouldSearchPersonasWithCombinationOfFilters() {
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
    @DisplayName("Should create multiple personas with different data")
    void shouldCreateMultiplePersonasWithDifferentData() {
        Persona p1 = new Persona();
        p1.setNombre("Juan");
        p1.setApellido("Pérez");
        p1.setNumeroIdentificacion("12345678");
        p1.setEsCliente(true);
        p1.setFkIdTipoIdentificacion(tipoIdentificacion);

        Persona p2 = new Persona();
        p2.setNombre("María");
        p2.setApellido("García");
        p2.setNumeroIdentificacion("87654321");
        p2.setEsCliente(false);
        p2.setFkIdTipoIdentificacion(tipoIdentificacion);

        Persona saved1 = personaService.save(p1);
        Persona saved2 = personaService.save(p2);

        assertThat(saved1.getIdPersona()).isNotNull();
        assertThat(saved2.getIdPersona()).isNotNull();
        assertThat(saved1.getIdPersona()).isNotEqualTo(saved2.getIdPersona());

        List<Persona> all = personaService.findAll();
        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved1.getIdPersona()))
                .anyMatch(p -> p.getIdPersona().equals(saved2.getIdPersona()));
    }

    @Test
    @DisplayName("Should update persona fields")
    void shouldUpdatePersonaFields() {
        Persona saved = personaService.save(testPersona);

        saved.setNombre("Carlos");
        saved.setApellido("López");
        saved.setEsCliente(true);
        Persona updated = personaService.save(saved);

        assertThat(updated)
                .hasFieldOrPropertyWithValue("nombre", "Carlos")
                .hasFieldOrPropertyWithValue("apellido", "López")
                .hasFieldOrPropertyWithValue("esCliente", true);
    }

    @Test
    @DisplayName("Should find persona by id even after update")
    void shouldFindPersonaByIdEvenAfterUpdate() {
        Persona saved = personaService.save(testPersona);
        Integer id = saved.getIdPersona();

        saved.setNombre("UpdatedName");
        personaService.save(saved);

        Optional<Persona> found = personaService.findById(id);

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getNombre()).isEqualTo("UpdatedName"));
    }

    @Test
    @DisplayName("Should search personas filtering by esCliente = false")
    void shouldSearchPersonasFilteringByEsClienteFalse() {
        Persona notClient = new Persona();
        notClient.setNombre("Abogado");
        notClient.setApellido("Penal");
        notClient.setNumeroIdentificacion("11111111");
        notClient.setEsCliente(false);
        notClient.setFkIdTipoIdentificacion(tipoIdentificacion);

        Persona saved = personaService.save(notClient);

        List<Persona> found = personaService.buscar(null, null, null, null, false);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPersona().equals(saved.getIdPersona())
                        && p.getEsCliente() == false);
    }
}
