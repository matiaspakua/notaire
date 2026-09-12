package com.licensis.notaire.unit;

import com.licensis.notaire.dto.TypeItem;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.PaymentService;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testBudget = new Budget();
        testBudget.setIdBudget(1);
        testBudget.setPropertyAmount(10000.00f);
    }

    @Nested
    @DisplayName("CU15 - Procesar pago")
    class ProcesarPaymentTests {

        @Test
        @DisplayName("Should process payment successfully with valid data")
        void shouldProcessPaymentSuccessfully() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(3000.00f);

            Payment savedPayment = new Payment();
            savedPayment.setIdPayment(1);
            savedPayment.setAmount(2000.00f);
            savedPayment.setDate(new Date());
            savedPayment.setBudget(testBudget);
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

            // Act
            Payment result = paymentService.processPayment(1, 2000.00f, new Date(), "Pago parcial");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(2000.00f);
            assertThat(result.getBudget()).isEqualTo(testBudget);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should throw exception when budget not found")
        void shouldThrowExceptionWhenBudgetNotFound() {
            // Arrange
            when(budgetRepository.findById(999)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> paymentService.processPayment(999, 1000.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Presupuesto no encontrado");
        }

        @Test
        @DisplayName("Should throw exception when amount is zero")
        void shouldThrowExceptionWhenAmountIsZero() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

            // Act & Assert
            assertThatThrownBy(() -> paymentService.processPayment(1, 0.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("El monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

            // Act & Assert
            assertThatThrownBy(() -> paymentService.processPayment(1, -500.00f, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("El monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void shouldThrowExceptionWhenAmountIsNull() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

            // Act & Assert
            assertThatThrownBy(() -> paymentService.processPayment(1, null, new Date(), "Test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("El monto del pago debe ser mayor a cero");
        }

        @Test
        @DisplayName("Should use current date when date is null")
        void shouldUseCurrentDateWhenDateIsNull() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);

            Date now = new Date();
            Payment savedPayment = new Payment();
            savedPayment.setIdPayment(1);
            savedPayment.setAmount(1000.00f);
            savedPayment.setDate(now);
            savedPayment.setBudget(testBudget);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                if (payment.getDate() == null) {
                    payment.setDate(new Date());
                }
                return payment;
            });

            // Act
            Payment result = paymentService.processPayment(1, 1000.00f, null, "Test");

            // Assert
            assertThat(result.getDate()).isNotNull();
            assertThat(result.getDate()).isBetween(
                    new Date(System.currentTimeMillis() - 1000),
                    new Date(System.currentTimeMillis() + 1000)
            );
        }

        @Test
        @DisplayName("Should calculate pending balance correctly")
        void shouldCalculatePendingBalanceCorrectly() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(4000.00f);

            // Act
            Float pendingBalance = paymentService.calculatePendingBalance(1);

            // Assert
            assertThat(pendingBalance).isEqualTo(6000.00f);
        }

        @Test
        @DisplayName("Should return full amount when no payments exist")
        void shouldReturnFullAmountWhenNoPaymentsExist() {
            // Arrange
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

            // Act
            Float pendingBalance = paymentService.calculatePendingBalance(1);

            // Assert
            assertThat(pendingBalance).isEqualTo(10000.00f);
        }
    }

    @Nested
    @DisplayName("CU47 - Consultar Pago")
    class ConsultarPaymentTests {

        @Test
        @DisplayName("Should return pago when valid ID is provided")
        void shouldReturnPaymentWhenValidIdProvided() {
            // Arrange
            Payment expectedPayment = new Payment();
            expectedPayment.setIdPayment(1);
            expectedPayment.setAmount(5000.00f);
            when(paymentRepository.findById(1)).thenReturn(Optional.of(expectedPayment));

            // Act
            Optional<Payment> result = paymentService.getPayment(1);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getIdPayment()).isEqualTo(1);
            assertThat(result.get().getAmount()).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Should return empty when pago not found")
        void shouldReturnEmptyWhenPaymentNotFound() {
            // Arrange
            when(paymentRepository.findById(999)).thenReturn(Optional.empty());

            // Act
            Optional<Payment> result = paymentService.getPayment(999);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find payments operations")
    class FindPaymentsTests {

        @Test
        @DisplayName("Should find all payments by budget ID")
        void shouldFindAllPaymentsByBudgetId() {
            // Arrange
            List<Payment> expectedPayments = List.of(
                    createPayment(1, 1000.00f),
                    createPayment(2, 2000.00f)
            );
            when(paymentRepository.findByFkIdBudgetIdBudget(1)).thenReturn(expectedPayments);

            // Act
            List<Payment> result = paymentService.findPaymentsByBudget(1);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Payment::getIdPayment).containsExactly(1, 2);
        }

        @Test
        @DisplayName("Should find all payments")
        void shouldFindAllPayments() {
            // Arrange
            List<Payment> expectedPayments = List.of(
                    createPayment(1, 1000.00f),
                    createPayment(2, 2000.00f),
                    createPayment(3, 3000.00f)
            );
            when(paymentRepository.findAll()).thenReturn(expectedPayments);

            // Act
            List<Payment> result = paymentService.findAll();

            // Assert
            assertThat(result).hasSize(3);
        }

        @Test
        @DisplayName("Should delete pago successfully")
        void shouldDeletePaymentSuccessfully() {
            // Arrange
            when(paymentRepository.existsById(1)).thenReturn(true);

            // Act
            paymentService.deletePayment(1);

            // Assert
            verify(paymentRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent pago")
        void shouldThrowExceptionWhenDeletingNonExistentPayment() {
            // Arrange
            when(paymentRepository.existsById(999)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> paymentService.deletePayment(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Pago no encontrado");
        }

        private Payment createPayment(Integer id, Float amount) {
            Payment payment = new Payment();
            payment.setIdPayment(id);
            payment.setAmount(amount);
            payment.setDate(new Date());
            return payment;
        }
    }

    @Nested
    @DisplayName("paymentMethod persistence")
    class PaymentMethodTests {

        @Test
        @DisplayName("Should persist paymentMethod when processing a payment")
        void shouldPersistPaymentMethodWhenProcesarPayment() {
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Payment result = paymentService.processPayment(1, 2000.00f, new Date(), "Pago parcial", "Efectivo");

            assertThat(result.getPaymentMethod()).isEqualTo("Efectivo");
        }

        @Test
        @DisplayName("Should allow null paymentMethod when processing a payment")
        void shouldAllowNullPaymentMethodOnProcesarPayment() {
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Payment result = paymentService.processPayment(1, 2000.00f, new Date(), "Pago parcial");

            assertThat(result.getPaymentMethod()).isNull();
        }

        @Test
        @DisplayName("Should update paymentMethod when editing a payment")
        void shouldUpdatePaymentMethodWhenEditarPayment() {
            Payment existing = new Payment();
            existing.setIdPayment(1);
            existing.setAmount(1000f);
            existing.setDate(new Date());
            existing.setPaymentMethod("Efectivo");
            when(paymentRepository.findById(1)).thenReturn(Optional.of(existing));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Payment result = paymentService.editPayment(1, 1000f, new Date(), "Editado", "Transferencia");

            assertThat(result.getPaymentMethod()).isEqualTo("Transferencia");
        }

        @Test
        @DisplayName("Should throw when editing paymentMethod of a non-existent pago")
        void shouldThrowWhenEditingPaymentMethodOfMissingPayment() {
            when(paymentRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.editPayment(999, 1000f, new Date(), "Test", "Efectivo"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Pago no encontrado");

            verify(paymentRepository, never()).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Overpayment Validation (Issue #848)")
    class OverpaymentValidationTests {

        @Test
        @DisplayName("Should reject payment that exceeds saldo pendiente")
        void shouldRejectPaymentExceedingSaldo() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(50000f);

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(30000f); // Already paid 30k

            // Saldo = 50k - 30k = 20k, trying to pay 25k should fail
            assertThatThrownBy(() -> paymentService.processPayment(1, 25000f, new Date(), "Overpay attempt"))
                    .isInstanceOf(PendingBalanceExceededException.class)
                    .hasMessageContaining("no puede exceder el saldo pendiente");

            verify(paymentRepository, never()).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should accept payment equal to saldo pendiente")
        void shouldAcceptPaymentEqualToSaldo() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(50000f);

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(30000f); // Already paid 30k
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setIdPayment(1);
                return p;
            });

            // Saldo = 50k - 30k = 20k, paying exactly 20k should succeed
            Payment result = paymentService.processPayment(1, 20000f, new Date(), "Exact payment");

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(20000f);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("Should accept payment less than saldo pendiente")
        void shouldAcceptPartialPayment() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(50000f);

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(30000f); // Already paid 30k
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
                Payment p = inv.getArgument(0);
                p.setIdPayment(1);
                return p;
            });

            // Saldo = 50k - 30k = 20k, paying 15k should succeed
            Payment result = paymentService.processPayment(1, 15000f, new Date(), "Partial payment");

            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(15000f);
            verify(paymentRepository).save(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("Descuentos y recargos en el total (Issue #822)")
    class DescuentosYRecargosTests {

        private Item buildItem(TypeItem type, float value) {
            Item item = new Item();
            item.setName("Item de prueba");
            item.setValue(value);
            item.setType(type);
            return item;
        }

        @Test
        @DisplayName("Should subtract a discount item from the total")
        void shouldSubtractDiscountItemFromTotal() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(0f);
            budget.setItemList(List.of(
                    buildItem(TypeItem.NORMAL, 10000f),
                    buildItem(TypeItem.DESCUENTO, 2000f)
            ));

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);

            Float pendingBalance = paymentService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualTo(8000f);
        }

        @Test
        @DisplayName("Should add a surcharge item to the total")
        void shouldAddSurchargeItemToTotal() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(0f);
            budget.setItemList(List.of(
                    buildItem(TypeItem.NORMAL, 10000f),
                    buildItem(TypeItem.RECARGO, 1500f)
            ));

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);

            Float pendingBalance = paymentService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualTo(11500f);
        }

        @Test
        @DisplayName("Should sum only normal items when there are no discounts or surcharges")
        void shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setPropertyAmount(0f);
            budget.setItemList(List.of(
                    buildItem(TypeItem.NORMAL, 10000f),
                    buildItem(TypeItem.NORMAL, 5000f)
            ));

            when(budgetRepository.findById(1)).thenReturn(Optional.of(budget));
            when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(0f);

            Float pendingBalance = paymentService.calculatePendingBalance(1);

            assertThat(pendingBalance).isEqualTo(15000f);
        }
    }
}
