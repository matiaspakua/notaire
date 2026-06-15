package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PresupuestoRepository;
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

@DisplayName("PresupuestoRepository Integration Tests")
class PresupuestoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private Presupuesto testPresupuesto;
    private Persona testPersona;
    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setNombre("DNI");
        tipoIdentificacionRepository.save(tipoIdentificacion);

        testPersona = new Persona();
        testPersona.setNombre("Cliente");
        testPersona.setApellido("Test");
        testPersona.setNumeroIdentificacion("12345678");
        testPersona.setEsCliente(true);
        testPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
        personaRepository.save(testPersona);

        testPresupuesto = new Presupuesto();
        testPresupuesto.setNumero((int) (System.currentTimeMillis() % 10000));
        testPresupuesto.setFecha(new Date());
        testPresupuesto.setEncabezado("Presupuesto Venta Inmueble");
        testPresupuesto.setEstado("PENDIENTE");
        testPresupuesto.setMontoInmueble(500000f);
        testPresupuesto.setFkIdPersona(testPersona);
    }

    @Test
    @DisplayName("Should persist and retrieve presupuesto")
    void shouldPersistAndRetrievePresupuesto() {
        Presupuesto saved = presupuestoRepository.save(testPresupuesto);

        assertThat(saved.getIdPresupuesto()).isNotNull();

        Optional<Presupuesto> retrieved = presupuestoRepository.findById(saved.getIdPresupuesto());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getNumero()).isEqualTo(saved.getNumero());
                    assertThat(p.getEncabezado()).isEqualTo("Presupuesto Venta Inmueble");
                    assertThat(p.getEstado()).isEqualTo("PENDIENTE");
                    assertThat(p.getMontoInmueble()).isEqualTo(500000f);
                });
    }

    @Test
    @DisplayName("Should find presupuesto by numero")
    void shouldFindByNumero() {
        Presupuesto saved = presupuestoRepository.save(testPresupuesto);

        Optional<Presupuesto> found = presupuestoRepository.findByNumero(saved.getNumero());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getEncabezado()).isEqualTo("Presupuesto Venta Inmueble"));
    }

    @Test
    @DisplayName("Should find presupuestos by persona")
    void shouldFindByPersona() {
        presupuestoRepository.save(testPresupuesto);

        List<Presupuesto> found = presupuestoRepository.findByFkIdPersonaIdPersona(testPersona.getIdPersona());

        assertThat(found).hasSize(1)
                .allMatch(p -> p.getFkIdPersona().getIdPersona().equals(testPersona.getIdPersona()));
    }

    @Test
    @DisplayName("Should find presupuestos by estado")
    void shouldFindByEstado() {
        presupuestoRepository.save(testPresupuesto);

        List<Presupuesto> found = presupuestoRepository.findByEstado("PENDIENTE");

        assertThat(found).isNotEmpty()
                .allMatch(p -> p.getEstado().equals("PENDIENTE"));
    }

    @Test
    @DisplayName("Should find presupuestos paginated")
    void shouldFindPresupuestosPaginated() {
        Presupuesto saved1 = presupuestoRepository.save(testPresupuesto);

        Presupuesto presupuesto2 = new Presupuesto();
        presupuesto2.setNumero((int) (System.currentTimeMillis() % 10000));
        presupuesto2.setFecha(new Date());
        presupuesto2.setEncabezado("Presupuesto 2");
        presupuesto2.setEstado("APROBADO");
        presupuesto2.setMontoInmueble(300000f);
        presupuesto2.setFkIdPersona(testPersona);
        presupuestoRepository.save(presupuesto2);

        Page<Presupuesto> page = presupuestoRepository.findAll(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should update presupuesto")
    void shouldUpdatePresupuesto() {
        Presupuesto saved = presupuestoRepository.save(testPresupuesto);

        saved.setEstado("APROBADO");
        saved.setMontoInmueble(550000f);
        presupuestoRepository.save(saved);

        Optional<Presupuesto> updated = presupuestoRepository.findById(saved.getIdPresupuesto());

        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getEstado()).isEqualTo("APROBADO");
                    assertThat(p.getMontoInmueble()).isEqualTo(550000f);
                });
    }

    @Test
    @DisplayName("Should delete presupuesto")
    void shouldDeletePresupuesto() {
        Presupuesto saved = presupuestoRepository.save(testPresupuesto);

        presupuestoRepository.deleteById(saved.getIdPresupuesto());

        Optional<Presupuesto> deleted = presupuestoRepository.findById(saved.getIdPresupuesto());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should maintain referential integrity with persona")
    void shouldMaintainReferentialIntegrityWithPersona() {
        Presupuesto saved = presupuestoRepository.save(testPresupuesto);

        Optional<Presupuesto> retrieved = presupuestoRepository.findById(saved.getIdPresupuesto());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getFkIdPersona()).isNotNull();
                    assertThat(p.getFkIdPersona().getIdPersona()).isEqualTo(testPersona.getIdPersona());
                    assertThat(p.getFkIdPersona().getNombre()).isEqualTo("Cliente");
                });
    }

    @Test
    @DisplayName("Should find presupuesto by estado ignoring case variations")
    void shouldFindByEstadoIgnoreCase() {
        presupuestoRepository.save(testPresupuesto);

        List<Presupuesto> found = presupuestoRepository.findByEstado("PENDIENTE");

        assertThat(found).isNotEmpty()
                .allMatch(p -> p.getEstado().equals("PENDIENTE"));
    }
}
