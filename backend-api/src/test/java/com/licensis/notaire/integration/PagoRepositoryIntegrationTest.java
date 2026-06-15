package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PagoRepository Integration Tests")
class PagoRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

    private Pago testPago;
    private Presupuesto testPresupuesto;
    private Persona testPersona;

    @BeforeEach
    void setUp() {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
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
        testPresupuesto.setEncabezado("Presupuesto Test");
        testPresupuesto.setEstado("PENDIENTE");
        testPresupuesto.setMontoInmueble(500000f);
        testPresupuesto.setFkIdPersona(testPersona);
        testPresupuesto = presupuestoRepository.save(testPresupuesto);

        testPago = new Pago();
        testPago.setMonto(100000f);
        testPago.setFecha(new Date());
        testPago.setPresupuesto(testPresupuesto);
    }

    @Test
    @DisplayName("Should persist and retrieve pago")
    void shouldPersistAndRetrievePago() {
        Pago saved = pagoRepository.save(testPago);

        assertThat(saved.getIdPago()).isNotNull();

        Optional<Pago> retrieved = pagoRepository.findById(saved.getIdPago());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getMonto()).isEqualTo(100000f);
                    assertThat(p.getFecha()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should find pagos by presupuesto")
    void shouldFindByPresupuesto() {
        pagoRepository.save(testPago);

        List<Pago> found = pagoRepository.findByFkIdPresupuestoIdPresupuesto(testPresupuesto.getIdPresupuesto());

        assertThat(found).hasSize(1)
                .allMatch(p -> p.getPresupuesto().getIdPresupuesto().equals(testPresupuesto.getIdPresupuesto()));
    }

    @Test
    @DisplayName("Should calculate sum of montos by presupuesto")
    void shouldSumMontoByPresupuesto() {
        pagoRepository.save(testPago);

        Pago pago2 = new Pago();
        pago2.setMonto(50000f);
        pago2.setFecha(new Date());
        pago2.setPresupuesto(testPresupuesto);
        pagoRepository.save(pago2);

        Float sum = pagoRepository.sumMontoByPresupuestoId(testPresupuesto.getIdPresupuesto());

        assertThat(sum).isEqualTo(150000f);
    }

    @Test
    @DisplayName("Should return null for sum when no pagos exist")
    void shouldReturnNullForSumWhenNoPagosExist() {
        Float sum = pagoRepository.sumMontoByPresupuestoId(9999);

        assertThat(sum).isNull();
    }

    @Test
    @DisplayName("Should find pagos by fecha range")
    void shouldFindByFechaRange() {
        pagoRepository.save(testPago);

        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);

        List<Pago> found = pagoRepository.findByFechaBetween(startDate, endDate);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPago().equals(testPago.getIdPago()));
    }

    @Test
    @DisplayName("Should update pago")
    void shouldUpdatePago() {
        Pago saved = pagoRepository.save(testPago);

        saved.setMonto(120000f);
        saved.setObservaciones("Pago actualizado");
        pagoRepository.save(saved);

        Optional<Pago> updated = pagoRepository.findById(saved.getIdPago());

        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getMonto()).isEqualTo(120000f);
                    assertThat(p.getObservaciones()).isEqualTo("Pago actualizado");
                });
    }

    @Test
    @DisplayName("Should delete pago")
    void shouldDeletePago() {
        Pago saved = pagoRepository.save(testPago);

        pagoRepository.deleteById(saved.getIdPago());

        Optional<Pago> deleted = pagoRepository.findById(saved.getIdPago());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should maintain referential integrity with presupuesto")
    void shouldMaintainReferentialIntegrityWithPresupuesto() {
        Pago saved = pagoRepository.save(testPago);

        Optional<Pago> retrieved = pagoRepository.findById(saved.getIdPago());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getPresupuesto()).isNotNull();
                    assertThat(p.getPresupuesto().getIdPresupuesto()).isEqualTo(testPresupuesto.getIdPresupuesto());
                });
    }

    @Test
    @DisplayName("Should find all pagos")
    void shouldFindAll() {
        pagoRepository.save(testPago);

        List<Pago> all = pagoRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getIdPago().equals(testPago.getIdPago()));
    }
}
