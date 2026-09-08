package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.EstadoDeGestionRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
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
    private PersonRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    @Autowired
    private EstadoDeGestionRepository estadoRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private TramiteRepository tramiteRepository;

    @Autowired
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    private GestionDeEscritura testGestion;
    private Person testEscribano;
    private EstadoDeGestion testEstado;

    @BeforeEach
    void setUp() {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("Profesional");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        testEscribano = new Person();
        testEscribano.setFirstName("Escribano");
        testEscribano.setLastName("Test");
        testEscribano.setIdentificationNumber("87654321");
        testEscribano.setIsClient(false);
        testEscribano.setFkIdIdentificationType(tipoIdentificacion);
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

        List<GestionDeEscritura> found = gestionRepository.findByFkIdPersonaEscribanoIdPersona(testEscribano.getPersonId());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdPersonaEscribano().getPersonId().equals(testEscribano.getPersonId()));
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
                        testEscribano.getPersonId(),
                        testEstado.getIdEstadoGestion());

        assertThat(found).isNotEmpty()
                .allMatch(g -> g.getFkIdPersonaEscribano().getPersonId().equals(testEscribano.getPersonId())
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
                    assertThat(g.getFkIdPersonaEscribano().getPersonId()).isEqualTo(testEscribano.getPersonId());
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

    @Test
    @DisplayName("Should find gestiones by cliente persona id (CU19)")
    void shouldFindByClientePersonaId() {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        Person cliente = new Person();
        cliente.setFirstName("Cliente");
        cliente.setLastName("Test");
        cliente.setIdentificationNumber("12345678");
        cliente.setIsClient(true);
        cliente.setFkIdIdentificationType(tipoIdentificacion);
        personaRepository.save(cliente);

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setNumero((int) (System.currentTimeMillis() % 10000));
        presupuesto.setFecha(new Date());
        presupuesto.setEncabezado("Presupuesto Test");
        presupuesto.setEstado("PENDIENTE");
        presupuesto.setFkIdPersona(cliente);
        presupuesto = presupuestoRepository.save(presupuesto);
        presupuestoRepository.flush();

        GestionDeEscritura saved = gestionRepository.save(testGestion);

        TipoDeTramite tipoDeTramite = new TipoDeTramite();
        tipoDeTramite.setNombre("Tipo Tramite Test");
        tipoDeTramiteRepository.save(tipoDeTramite);

        Tramite tramite = new Tramite();
        tramite.setFkIdPresupuesto(presupuesto);
        tramite.setFkIdGestion(saved);
        tramite.setFkIdTipoTramite(tipoDeTramite);
        tramiteRepository.save(tramite);

        List<GestionDeEscritura> found = gestionRepository.findByClientePersonaId(cliente.getPersonId());

        assertThat(found).isNotEmpty()
                .anyMatch(g -> g.getIdGestion().equals(saved.getIdGestion()));
    }
}
