package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU15", "CU47"})
@DisplayName("PagoService Tests")
@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private PagoService pagoService;

    private Presupuesto testPresupuesto;

    @BeforeEach
    void setUp() {
        testPresupuesto = new Presupuesto();
        testPresupuesto.setIdPresupuesto(1);
        testPresupuesto.setMontoInmueble(10000.00f);
    }

    @Nested
    @DisplayName("CU15 - Procesar pago")
    class ProcesarPagoTests {

        @Test
        @DisplayName("Should process payment successfully with valid data")
        void shouldProcessPaymentSuccessfully() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(3000.00f);

            Pago savedPago = new Pago();
            savedPago.setIdPago(1);
            savedPago.setMonto(2000.00f);
            savedPago.setFecha(new Date());
            savedPago.setPresupuesto(testPresupuesto);
            when(pagoRepository.save(any(Pago.class))).thenReturn(savedPago);

            // Act
            Pago result = pagoService.procesarPago(1, 2000.00f, new Date(), "Pago parcial");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getMonto()).isEqualTo(2000.00f);
            assertThat(result.getPresupuesto()).isEqualTo(testPresupuesto);
            verify(pagoRepository).save(any(Pago.class));
        }

        @Test
        @DisplayName("Should throw exception when presupuesto not found")
        void shouldThrowExceptionWhenPresupuestoNotFound() {
            // Arrange
            when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> pagoService.procesarPago(999, 1000.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presupuesto no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when monto is zero")
        void shouldThrowExceptionWhenMontoIsZero() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

            // Act & Assert
            assertThatThrownBy(() -> pagoService.procesarPago(1, 0.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should throw exception when monto is negative")
        void shouldThrowExceptionWhenMontoIsNegative() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

            // Act & Assert
            assertThatThrownBy(() -> pagoService.procesarPago(1, -500.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should throw exception when monto is null")
        void shouldThrowExceptionWhenMontoIsNull() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

            // Act & Assert
            assertThatThrownBy(() -> pagoService.procesarPago(1, null, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should use current date when fecha is null")
        void shouldUseCurrentDateWhenFechaIsNull() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(0f);

            Date now = new Date();
            Pago savedPago = new Pago();
            savedPago.setIdPago(1);
            savedPago.setMonto(1000.00f);
            savedPago.setFecha(now);
            savedPago.setPresupuesto(testPresupuesto);
            when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> {
                Pago pago = invocation.getArgument(0);
                if (pago.getFecha() == null) {
                    pago.setFecha(new Date());
                }
                return pago;
            });

            // Act
            Pago result = pagoService.procesarPago(1, 1000.00f, null, "Test");

            // Assert
            assertThat(result.getFecha()).isNotNull();
            assertThat(result.getFecha()).isBetween(
                    new Date(System.currentTimeMillis() - 1000),
                    new Date(System.currentTimeMillis() + 1000)
            );
        }

        @Test
        @DisplayName("Should calculate pending balance correctly")
        void shouldCalculatePendingBalanceCorrectly() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(4000.00f);

            // Act
            Float saldoPendiente = pagoService.calcularSaldoPendiente(1);

            // Assert
            assertThat(saldoPendiente).isEqualTo(6000.00f);
        }

        @Test
        @DisplayName("Should return full amount when no payments exist")
        void shouldReturnFullAmountWhenNoPaymentsExist() {
            // Arrange
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(null);

            // Act
            Float saldoPendiente = pagoService.calcularSaldoPendiente(1);

            // Assert
            assertThat(saldoPendiente).isEqualTo(10000.00f);
        }
    }

    @Nested
    @DisplayName("CU47 - Consultar Pago")
    class ConsultarPagoTests {

        @Test
        @DisplayName("Should return pago when valid ID is provided")
        void shouldReturnPagoWhenValidIdProvided() {
            // Arrange
            Pago expectedPago = new Pago();
            expectedPago.setIdPago(1);
            expectedPago.setMonto(5000.00f);
            when(pagoRepository.findById(1)).thenReturn(Optional.of(expectedPago));

            // Act
            Optional<Pago> result = pagoService.consultarPago(1);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getIdPago()).isEqualTo(1);
            assertThat(result.get().getMonto()).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Should return empty when pago not found")
        void shouldReturnEmptyWhenPagoNotFound() {
            // Arrange
            when(pagoRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<Pago> result = pagoService.consultarPago(999);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find pagos operations")
    class FindPagosTests {

        @Test
        @DisplayName("Should find all pagos by presupuesto ID")
        void shouldFindAllPagosByPresupuestoId() {
            // Arrange
            List<Pago> expectedPagos = List.of(
                    createPago(1, 1000.00f),
                    createPago(2, 2000.00f)
            );
            when(pagoRepository.findByFkIdPresupuestoIdPresupuesto(1)).thenReturn(expectedPagos);

            // Act
            List<Pago> result = pagoService.findPagosByPresupuesto(1);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Pago::getIdPago).containsExactly(1, 2);
        }

        @Test
        @DisplayName("Should find all pagos")
        void shouldFindAllPagos() {
            // Arrange
            List<Pago> expectedPagos = List.of(
                    createPago(1, 1000.00f),
                    createPago(2, 2000.00f),
                    createPago(3, 3000.00f)
            );
            when(pagoRepository.findAll()).thenReturn(expectedPagos);

            // Act
            List<Pago> result = pagoService.findAll();

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should delete pago successfully")
        void shouldDeletePagoSuccessfully() {
            // Arrange
            when(pagoRepository.existsById(1)).thenReturn(true);

            // Act
            pagoService.deletePago(1);

            // Assert
            verify(pagoRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent pago")
        void shouldThrowExceptionWhenDeletingNonExistentPago() {
            // Arrange
            when(pagoRepository.existsById(999)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> pagoService.deletePago(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Pago no encontrado");
        }

        private Pago createPago(Integer id, Float monto) {
            Pago pago = new Pago();
            pago.setIdPago(id);
            pago.setMonto(monto);
            pago.setFecha(new Date());
            return pago;
        }
    }

    @Nested
    @DisplayName("metodoPago persistence")
    class MetodoPagoTests {

        @Test
        @DisplayName("Should persist metodoPago when processing a payment")
        void shouldPersistMetodoPagoWhenProcesarPago() {
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(0f);
            when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pago result = pagoService.procesarPago(1, 2000.00f, new Date(), "Pago parcial", "Efectivo");

            assertThat(result.getMetodoPago()).isEqualTo("Efectivo");
        }

        @Test
        @DisplayName("Should allow null metodoPago when processing a payment")
        void shouldAllowNullMetodoPagoOnProcesarPago() {
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));
            when(pagoRepository.sumMontoByPresupuestoId(1)).thenReturn(0f);
            when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pago result = pagoService.procesarPago(1, 2000.00f, new Date(), "Pago parcial");

            assertThat(result.getMetodoPago()).isNull();
        }

        @Test
        @DisplayName("Should update metodoPago when editing a payment")
        void shouldUpdateMetodoPagoWhenEditarPago() {
            Pago existing = new Pago();
            existing.setIdPago(1);
            existing.setMonto(1000f);
            existing.setFecha(new Date());
            existing.setMetodoPago("Efectivo");
            when(pagoRepository.findById(1)).thenReturn(Optional.of(existing));
            when(pagoRepository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pago result = pagoService.editarPago(1, 1000f, new Date(), "Editado", "Transferencia");

            assertThat(result.getMetodoPago()).isEqualTo("Transferencia");
        }

        @Test
        @DisplayName("Should throw when editing metodoPago of a non-existent pago")
        void shouldThrowWhenEditingMetodoPagoOfMissingPago() {
            when(pagoRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pagoService.editarPago(999, 1000f, new Date(), "Test", "Efectivo"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Pago no encontrado");

            verify(pagoRepository, never()).save(any(Pago.class));
        }
    }
}
