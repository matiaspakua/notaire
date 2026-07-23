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

    @Test
    @DisplayName("Should handle editarPago with null monto")
    void shouldHandleEditarPagoWithNullMonto() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago Original"
        );

        Pago edited = pagoService.editarPago(saved.getIdPago(), null, new Date(), "Updated");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("monto", 100000f)
                .hasFieldOrPropertyWithValue("observaciones", "Updated");
    }

    @Test
    @DisplayName("Should handle editarPago with null fecha")
    void shouldHandleEditarPagoWithNullFecha() {
        Date originalDate = new Date();
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                originalDate,
                "Pago"
        );

        Pago edited = pagoService.editarPago(saved.getIdPago(), 120000f, null, "Updated");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("monto", 120000f)
                .hasFieldOrPropertyWithValue("fecha", originalDate);
    }

    @Test
    @DisplayName("Should handle editarPago with null observaciones")
    void shouldHandleEditarPagoWithNullObservaciones() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Original"
        );

        Pago edited = pagoService.editarPago(saved.getIdPago(), 120000f, new Date(), null);

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("monto", 120000f)
                .hasFieldOrPropertyWithValue("observaciones", "Original");
    }

    @Test
    @DisplayName("Should reject negative monto in editarPago")
    void shouldRejectNegativeMontoInEditarPago() {
        Pago saved = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                new Date(),
                "Pago"
        );

        assertThatThrownBy(() -> pagoService.editarPago(
                saved.getIdPago(),
                -50000f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent pago")
    void shouldThrowExceptionWhenEditingNonExistentPago() {
        assertThatThrownBy(() -> pagoService.editarPago(
                9999,
                100000f,
                new Date(),
                "Test"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pago")
    void shouldThrowExceptionWhenDeletingNonExistentPago() {
        assertThatThrownBy(() -> pagoService.deletePago(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("Should handle procesarPago with null fecha and use current date")
    void shouldHandleProcessPagoWithNullFecha() {
        long beforeTime = System.currentTimeMillis();

        Pago result = pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                100000f,
                null,
                "Pago"
        );

        long afterTime = System.currentTimeMillis();

        assertThat(result).isNotNull();
        assertThat(result.getFecha()).isNotNull();
        assertThat(result.getFecha().getTime()).isBetween(beforeTime, afterTime);
    }

    @Test
    @DisplayName("Should handle zero monto in procesarPago")
    void shouldRejectZeroMontoInProcessarPago() {
        assertThatThrownBy(() -> pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                0f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    @DisplayName("Should handle multiple pagos with different amounts")
    void shouldHandleMultiplePagosWithDifferentAmounts() {
        float monto1 = 50000f;
        float monto2 = 75000f;
        float monto3 = 125000f;

        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), monto1, new Date(), "Pago 1");
        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), monto2, new Date(), "Pago 2");
        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), monto3, new Date(), "Pago 3");

        List<Pago> pagos = pagoService.findPagosByPresupuesto(testPresupuesto.getIdPresupuesto());

        assertThat(pagos)
                .hasSize(3)
                .extracting(Pago::getMonto)
                .containsExactlyInAnyOrder(monto1, monto2, monto3);

        Float saldoPendiente = pagoService.calcularSaldoPendiente(testPresupuesto.getIdPresupuesto());
        float totalPagado = monto1 + monto2 + monto3;
        assertThat(saldoPendiente).isEqualTo(500000f - totalPagado);
    }

    @Test
    @DisplayName("Should find pagos by fecha range with multiple entries")
    void shouldFindPagosByFechaRangeWithMultipleEntries() {
        long now = System.currentTimeMillis();
        Date date1 = new Date(now - 2 * 86400000);
        Date date2 = new Date(now - 86400000);
        Date date3 = new Date(now);

        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), 100000f, date1, "Pago 1");
        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), 100000f, date2, "Pago 2");
        pagoService.procesarPago(testPresupuesto.getIdPresupuesto(), 100000f, date3, "Pago 3");

        Date startDate = new Date(now - 3 * 86400000);
        Date endDate = new Date(now + 86400000);

        List<Pago> found = pagoService.findPagosByFechaRange(startDate, endDate);

        List<Pago> foundDePresupuesto = found.stream()
                .filter(p -> p.getPresupuesto() != null
                        && testPresupuesto.getIdPresupuesto().equals(p.getPresupuesto().getIdPresupuesto()))
                .toList();
        assertThat(foundDePresupuesto).hasSize(3);
    }

    @Test
    @DisplayName("Should calculate saldo correctly with full payment")
    void shouldCalculateSaldoWithFullPayment() {
        pagoService.procesarPago(
                testPresupuesto.getIdPresupuesto(),
                500000f,
                new Date(),
                "Full payment"
        );

        Float saldoPendiente = pagoService.calcularSaldoPendiente(testPresupuesto.getIdPresupuesto());
        assertThat(saldoPendiente).isZero();
    }

    @Test
    @DisplayName("Should throw exception when calcularSaldoPendiente for non-existent presupuesto")
    void shouldThrowExceptionWhenCalculatingSaldoForNonExistentPresupuesto() {
        assertThatThrownBy(() -> pagoService.calcularSaldoPendiente(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }
}
