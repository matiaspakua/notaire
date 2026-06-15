package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TipoIdentificacionRepository;
import com.licensis.notaire.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PagoService Integration Tests")
class PagoServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private TipoIdentificacionRepository tipoIdentificacionRepository;

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
        testPersona = personaRepository.save(testPersona);

        testPresupuesto = new Presupuesto();
        testPresupuesto.setNumero((int) (System.currentTimeMillis() % 10000));
        testPresupuesto.setFecha(new Date());
        testPresupuesto.setEncabezado("Presupuesto Test");
        testPresupuesto.setEstado("PENDIENTE");
        testPresupuesto.setMontoInmueble(500000f);
        testPresupuesto.setFkIdPersona(testPersona);
        testPresupuesto = presupuestoRepository.save(testPresupuesto);
    }

    @Test
    @DisplayName("Should process valid pago through service")
    void shouldProcessValidPagoThroughService() {
        Pago result = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Primer pago"
        );

        assertThat(result).isNotNull();
        assertThat(result.getIdPago()).isNotNull();
        assertThat(result.getMonto()).isEqualTo(100000f);
    }

    @Test
    @DisplayName("Should throw exception when presupuesto not found")
    void shouldThrowExceptionWhenPresupuestoNotFound() {
        assertThatThrownBy(() -> pagoService.procesarPago(9999, 100000f, new Date(), "Test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente correctly")
    void shouldCalculateSaldoPendienteCorrectly() {
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago 1"
        );

        Float saldoPendiente = pagoService.calcularSaldoPendiente(testPresupuesto.getIdPresupuesto());

        assertThat(saldoPendiente).isEqualTo(400000f);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente with multiple pagos")
    void shouldCalculateSaldoPendienteWithMultiplePagos() {
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago 1"
        );
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                150000f,
                new Date(),
                "Pago 2"
        );

        Float saldoPendiente = pagoService.calcularSaldoPendiente(testPresupuesto.getIdPresupuesto());

        assertThat(saldoPendiente).isEqualTo(250000f);
    }

    @Test
    @DisplayName("Should find pagos by presupuesto through service")
    void shouldFindPagosByPresupuestoThroughService() {
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago 1"
        );

        List<Pago> found = pagoService.findPagosByPresupuesto(testPresupuesto.getIdPresupuesto());

        assertThat(found).isNotEmpty()
                .hasSize(1)
                .allMatch(p -> p.getPresupuesto().getIdPresupuesto().equals(testPresupuesto.getIdPresupuesto()));
    }

    @Test
    @DisplayName("Should find all pagos through service")
    void shouldFindAllPagosThroughService() {
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        List<Pago> all = pagoService.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should find pago by id through service")
    void shouldFindPagoByIdThroughService() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        Optional<Pago> found = pagoService.consultarPago(saved.getIdPago());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getMonto()).isEqualTo(100000f));
    }

    @Test
    @DisplayName("Should find pagos by fecha range through service")
    void shouldFindPagosByFechaRangeThroughService() {
        Date now = new Date();
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                now,
                "Pago"
        );

        Date startDate = new Date(now.getTime() - 86400000);
        Date endDate = new Date(now.getTime() + 86400000);

        List<Pago> found = pagoService.findPagosByFechaRange(startDate, endDate);

        assertThat(found).isNotEmpty();
    }

    @Test
    @DisplayName("Should delete pago through service")
    void shouldDeletePagoThroughService() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        pagoService.deletePago(saved.getIdPago());

        Optional<Pago> deleted = pagoService.consultarPago(saved.getIdPago());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should edit pago through service")
    void shouldEditPagoThroughService() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        Pago edited = pagoService.editarPago(saved.getIdPago(), 120000f, new Date(), "Editado");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("monto", 120000f);
    }

    @Test
    @DisplayName("Should enforce monto validation in service")
    void shouldEnforcMontoValidationInService() {
        assertThatThrownBy(() -> pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                -100f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should maintain transaction consistency across service methods")
    void shouldMaintainTransactionConsistency() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        pagoService.deletePago(saved.getIdPago());

        List<Pago> pagos = pagoService.findPagosByPresupuesto(testPresupuesto.getIdPresupuesto());
        assertThat(pagos).isEmpty();
    }
}
