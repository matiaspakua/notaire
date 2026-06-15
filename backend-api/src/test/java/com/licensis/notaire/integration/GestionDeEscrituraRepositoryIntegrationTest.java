package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GestionDeEscrituraRepository Integration Tests")
class GestionDeEscrituraRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private GestionDeEscrituraRepository gestionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    @Autowired
    private EstadoDeGestionRepository estadoRepository;

    private GestionDeEscritura testGestion;
    private Persona testEscribano;
    private EstadoDeGestion testEstado;

    @BeforeEach
    void setUp() {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("Profesional");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        testEscribano = new Persona();
        testEscribano.setNombre("Escribano");
        testEscribano.setApellido("Test");
        testEscribano.setNumeroIdentificacion("87654321");
        testEscribano.setEsCliente(false);
        testEscribano.setFkIdTipoIdentificacion(tipoIdentificacion);
        personaRepository.save(testEscribano);

        testEstado = new EstadoDeGestion();
        testEstado.setNombre("INICIADO");
        testEstado = estadoRepository.save(testEstado);

        testGestion = new GestionDeEscritura();
        testGestion.setNumero((int) (System.currentTimeMillis() % 10000));
        testGestion.setEncabezado("Venta Inmueble");
        testGestion.setFechaInicio(new Date());
        testGestion.setFkIdPersonaEscribano(testEscribano);
        testGestion.setFkIdEstadoDeGestion(testEstado);
    }

    @Test
    @DisplayName("Should persist and retrieve gestion")
    void shouldPersistAndRetrieveGestion() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        assertThat(saved.getIdGestion()).isNotNull();

        Optional<GestionDeEscritura> retrieved = gestionRepository.findById(saved.getIdGestion());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getNumero()).isEqualTo(saved.getNumero());
                    assertThat(g.getEncabezado()).isEqualTo("Venta Inmueble");
                });
    }

    @Test
    @DisplayName("Should find gestion by numero")
    void shouldFindByNumero() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        Optional<GestionDeEscritura> found = gestionRepository.findByNumero(saved.getNumero());

        assertThat(found).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getEncabezado()).isEqualTo("Venta Inmueble"));
    }

    @Test
    @DisplayName("Should find gestiones by escribano")
    void shouldFindByEscribano() {
        gestionRepository.save(testGestion);

        List<GestionDeEscritura> found = gestionRepository.findByFkIdPersonaEscribanoIdPersona(testEscribano.getIdPersona());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdPersonaEscribano().getIdPersona().equals(testEscribano.getIdPersona()));
    }

    @Test
    @DisplayName("Should find gestiones by estado")
    void shouldFindByEstado() {
        gestionRepository.save(testGestion);

        List<GestionDeEscritura> found = gestionRepository.findByFkIdEstadoDeGestionIdEstadoGestion(testEstado.getIdEstadoGestion());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdEstadoDeGestion().getIdEstadoGestion().equals(testEstado.getIdEstadoGestion()));
    }

    @Test
    @DisplayName("Should find gestiones by escribano and estado")
    void shouldFindByEscribanoAndEstado() {
        gestionRepository.save(testGestion);

        List<GestionDeEscritura> found = gestionRepository
                .findByFkIdPersonaEscribanoIdPersonaAndFkIdEstadoIdEstadoGestion(
                        testEscribano.getIdPersona(),
                        testEstado.getIdEstadoGestion());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdPersonaEscribano().getIdPersona().equals(testEscribano.getIdPersona())
                        && g.getFkIdEstadoDeGestion().getIdEstadoGestion().equals(testEstado.getIdEstadoGestion()));
    }

    @Test
    @DisplayName("Should find gestiones by fecha range")
    void shouldFindByFechaInicioBetween() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        Date startDate = new Date(saved.getFechaInicio().getTime() - 86400000);
        Date endDate = new Date(saved.getFechaInicio().getTime() + 86400000);

        List<GestionDeEscritura> found = gestionRepository.findByFechaInicioBetween(startDate, endDate);

        assertThat(found).isNotEmpty()
                .anyMatch(g -> g.getIdGestion().equals(saved.getIdGestion()));
    }

    @Test
    @DisplayName("Should find gestiones by observaciones containing")
    void shouldFindByObservacionesContaining() {
        testGestion.setObservaciones("Venta de propiedad");
        gestionRepository.save(testGestion);

        List<GestionDeEscritura> found = gestionRepository.findByObservacionesContaining("propiedad");

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getObservaciones().contains("propiedad"));
    }

    @Test
    @DisplayName("Should update gestion")
    void shouldUpdateGestion() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        saved.setObservaciones("Actualizado");
        gestionRepository.save(saved);

        Optional<GestionDeEscritura> updated = gestionRepository.findById(saved.getIdGestion());

        assertThat(updated).isPresent()
                .hasValueSatisfying(g -> assertThat(g.getObservaciones()).isEqualTo("Actualizado"));
    }

    @Test
    @DisplayName("Should delete gestion")
    void shouldDeleteGestion() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        gestionRepository.deleteById(saved.getIdGestion());

        Optional<GestionDeEscritura> deleted = gestionRepository.findById(saved.getIdGestion());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should maintain referential integrity with escribano")
    void shouldMaintainReferentialIntegrityWithEscribano() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        Optional<GestionDeEscritura> retrieved = gestionRepository.findById(saved.getIdGestion());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getFkIdPersonaEscribano()).isNotNull();
                    assertThat(g.getFkIdPersonaEscribano().getIdPersona()).isEqualTo(testEscribano.getIdPersona());
                });
    }

    @Test
    @DisplayName("Should find all gestiones paginated")
    void shouldFindAllPaginated() {
        GestionDeEscritura saved = gestionRepository.save(testGestion);

        Page<GestionDeEscritura> page = gestionRepository.findAll(PageRequest.of(0, 10));

        assertThat(page).isNotEmpty()
                .anyMatch(g -> g.getIdGestion().equals(saved.getIdGestion()));
    }
}
