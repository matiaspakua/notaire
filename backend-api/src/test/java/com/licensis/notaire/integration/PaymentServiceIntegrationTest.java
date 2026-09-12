package com.licensis.notaire.integration;

import com.licensis.notaire.exception.PendingBalanceExceededException;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PagoService Integration Tests")
class PaymentServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Budget testBudget;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationTypeRepository.save(identificationType);

        testPerson = new Person();
        testPerson.setFirstName("Cliente");
        testPerson.setLastName("Test");
        testPerson.setIdentificationNumber("12345678");
        testPerson.setIsClient(true);
        testPerson.setFkIdIdentificationType(identificationType);
        testPerson = personRepository.save(testPerson);

        testBudget = new Budget();
        testBudget.setNumber((int) (System.currentTimeMillis() % 10000));
        testBudget.setDate(new Date());
        testBudget.setEncabezado("Presupuesto Test");
        testBudget.setStatus("PENDIENTE");
        testBudget.setPropertyAmount(500000f);
        testBudget.setFkIdPerson(testPerson);
        testBudget = budgetRepository.save(testBudget);
    }

    @Test
    @DisplayName("Should process valid pago through service")
    void shouldProcessValidPaymentThroughService() {
        Payment result = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Primer pago"
        );

        assertThat(result).isNotNull();
        assertThat(result.getIdPayment()).isNotNull();
        assertThat(result.getAmount()).isEqualTo(100000f);
    }

    @Test
    @DisplayName("Should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        assertThatThrownBy(() -> paymentService.processPayment(9999, 100000f, new Date(), "Test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente correctly")
    void shouldCalculateSaldoPendingCorrectly() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago 1"
        );

        Float pendingBalance = paymentService.calculatePendingBalance(testBudget.getIdBudget());

        assertThat(pendingBalance).isEqualTo(400000f);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente with multiple payments")
    void shouldCalculateSaldoPendingWithMultiplePayments() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago 1"
        );
        paymentService.processPayment(
                testBudget.getIdBudget(),
                150000f,
                new Date(),
                "Pago 2"
        );

        Float pendingBalance = paymentService.calculatePendingBalance(testBudget.getIdBudget());

        assertThat(pendingBalance).isEqualTo(250000f);
    }

    @Test
    @DisplayName("Should reject a pago exceeding saldo pendiente and not persist it")
    void shouldRejectPaymentExceedingSaldoPending() {
        assertThatThrownBy(() -> paymentService.processPayment(
                testBudget.getIdBudget(),
                600000f,
                new Date(),
                "Overpay attempt"
        )).isInstanceOf(PendingBalanceExceededException.class);

        List<Payment> payments = paymentService.findPaymentsByBudget(testBudget.getIdBudget());
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should reject a pago exceeding saldo already reduced by a prior payment")
    void shouldRejectPaymentExceedingSaldoReducedByPriorPayment() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                400000f,
                new Date(),
                "Pago 1"
        );

        assertThatThrownBy(() -> paymentService.processPayment(
                testBudget.getIdBudget(),
                150000f,
                new Date(),
                "Overpay against reduced saldo"
        )).isInstanceOf(PendingBalanceExceededException.class);

        List<Payment> payments = paymentService.findPaymentsByBudget(testBudget.getIdBudget());
        assertThat(payments).hasSize(1);
    }

    @Test
    @DisplayName("Should find payments by budget through service")
    void shouldFindPaymentsByBudgetThroughService() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago 1"
        );

        List<Payment> found = paymentService.findPaymentsByBudget(testBudget.getIdBudget());

        assertThat(found).isNotEmpty()
                .hasSize(1)
                .allMatch(p -> p.getBudget().getIdBudget().equals(testBudget.getIdBudget()));
    }

    @Test
    @DisplayName("Should find all payments through service")
    void shouldFindAllPaymentsThroughService() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        List<Payment> all = paymentService.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should find pago by id through service")
    void shouldFindPaymentByIdThroughService() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        Optional<Payment> found = paymentService.getPayment(saved.getIdPayment());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.getAmount()).isEqualTo(100000f));
    }

    @Test
    @DisplayName("Should find payments by date range through service")
    void shouldFindPaymentsByDateRangeThroughService() {
        Date now = new Date();
        paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                now,
                "Pago"
        );

        Date startDate = new Date(now.getTime() - 86400000);
        Date endDate = new Date(now.getTime() + 86400000);

        List<Payment> found = paymentService.findPaymentsByDateRange(startDate, endDate);

        assertThat(found).isNotEmpty();
    }

    @Test
    @DisplayName("Should delete pago through service")
    void shouldDeletePaymentThroughService() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        paymentService.deletePayment(saved.getIdPayment());

        Optional<Payment> deleted = paymentService.getPayment(saved.getIdPayment());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should edit pago through service")
    void shouldEditPaymentThroughService() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        Payment edited = paymentService.editPayment(saved.getIdPayment(), 120000f, new Date(), "Editado");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("amount", 120000f);
    }

    @Test
    @DisplayName("Should enforce amount validation in service")
    void shouldEnforcAmountValidationInService() {
        assertThatThrownBy(() -> paymentService.processPayment(
                testBudget.getIdBudget(),
                -100f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should maintain transaction consistency across service methods")
    void shouldMaintainTransactionConsistency() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        paymentService.deletePayment(saved.getIdPayment());

        List<Payment> payments = paymentService.findPaymentsByBudget(testBudget.getIdBudget());
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should handle editarPago with null amount")
    void shouldHandleEditarPaymentWithNullAmount() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago Original"
        );

        Payment edited = paymentService.editPayment(saved.getIdPayment(), null, new Date(), "Updated");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("amount", 100000f)
                .hasFieldOrPropertyWithValue("notes", "Updated");
    }

    @Test
    @DisplayName("Should handle editarPago with null date")
    void shouldHandleEditarPaymentWithNullDate() {
        Date originalDate = new Date();
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                originalDate,
                "Pago"
        );

        Payment edited = paymentService.editPayment(saved.getIdPayment(), 120000f, null, "Updated");

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("amount", 120000f)
                .hasFieldOrPropertyWithValue("date", originalDate);
    }

    @Test
    @DisplayName("Should handle editarPago with null notes")
    void shouldHandleEditarPaymentWithNullNotes() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Original"
        );

        Payment edited = paymentService.editPayment(saved.getIdPayment(), 120000f, new Date(), null);

        assertThat(edited).isNotNull()
                .hasFieldOrPropertyWithValue("amount", 120000f)
                .hasFieldOrPropertyWithValue("notes", "Original");
    }

    @Test
    @DisplayName("Should reject negative amount in editarPago")
    void shouldRejectNegativeAmountInEditarPayment() {
        Payment saved = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                new Date(),
                "Pago"
        );

        assertThatThrownBy(() -> paymentService.editPayment(
                saved.getIdPayment(),
                -50000f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent pago")
    void shouldThrowExceptionWhenEditingNonExistentPayment() {
        assertThatThrownBy(() -> paymentService.editPayment(
                9999,
                100000f,
                new Date(),
                "Test"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pago")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        assertThatThrownBy(() -> paymentService.deletePayment(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    @DisplayName("Should handle procesarPago with null date and use current date")
    void shouldHandleProcessPaymentWithNullDate() {
        long beforeTime = System.currentTimeMillis();

        Payment result = paymentService.processPayment(
                testBudget.getIdBudget(),
                100000f,
                null,
                "Pago"
        );

        long afterTime = System.currentTimeMillis();

        assertThat(result).isNotNull();
        assertThat(result.getDate()).isNotNull();
        assertThat(result.getDate().getTime()).isBetween(beforeTime, afterTime);
    }

    @Test
    @DisplayName("Should handle zero amount in procesarPago")
    void shouldRejectZeroAmountInProcessarPayment() {
        assertThatThrownBy(() -> paymentService.processPayment(
                testBudget.getIdBudget(),
                0f,
                new Date(),
                "Invalid"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor a cero");
    }

    @Test
    @DisplayName("Should handle multiple payments with different amounts")
    void shouldHandleMultiplePaymentsWithDifferentAmounts() {
        float monto1 = 50000f;
        float monto2 = 75000f;
        float monto3 = 125000f;

        paymentService.processPayment(testBudget.getIdBudget(), monto1, new Date(), "Pago 1");
        paymentService.processPayment(testBudget.getIdBudget(), monto2, new Date(), "Pago 2");
        paymentService.processPayment(testBudget.getIdBudget(), monto3, new Date(), "Pago 3");

        List<Payment> payments = paymentService.findPaymentsByBudget(testBudget.getIdBudget());

        assertThat(payments)
                .hasSize(3)
                .extracting(Payment::getAmount)
                .containsExactlyInAnyOrder(monto1, monto2, monto3);

        Float pendingBalance = paymentService.calculatePendingBalance(testBudget.getIdBudget());
        float totalPaid = monto1 + monto2 + monto3;
        assertThat(pendingBalance).isEqualTo(500000f - totalPaid);
    }

    @Test
    @DisplayName("Should find payments by date range with multiple entries")
    void shouldFindPaymentsByDateRangeWithMultipleEntries() {
        long now = System.currentTimeMillis();
        Date date1 = new Date(now - 2 * 86400000);
        Date date2 = new Date(now - 86400000);
        Date date3 = new Date(now);

        paymentService.processPayment(testBudget.getIdBudget(), 100000f, date1, "Pago 1");
        paymentService.processPayment(testBudget.getIdBudget(), 100000f, date2, "Pago 2");
        paymentService.processPayment(testBudget.getIdBudget(), 100000f, date3, "Pago 3");

        Date startDate = new Date(now - 3 * 86400000);
        Date endDate = new Date(now + 86400000);

        List<Payment> found = paymentService.findPaymentsByDateRange(startDate, endDate);

        List<Payment> foundDeBudget = found.stream()
                .filter(p -> p.getBudget() != null
                        && testBudget.getIdBudget().equals(p.getBudget().getIdBudget()))
                .toList();
        assertThat(foundDeBudget).hasSize(3);
    }

    @Test
    @DisplayName("Should calculate saldo correctly with full payment")
    void shouldCalculateSaldoWithFullPayment() {
        paymentService.processPayment(
                testBudget.getIdBudget(),
                500000f,
                new Date(),
                "Full payment"
        );

        Float pendingBalance = paymentService.calculatePendingBalance(testBudget.getIdBudget());
        assertThat(pendingBalance).isZero();
    }

    @Test
    @DisplayName("Should throw exception when calcularSaldoPendiente for non-existent budget")
    void shouldThrowExceptionWhenCalculatingSaldoForNonExistentBudget() {
        assertThatThrownBy(() -> paymentService.calculatePendingBalance(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrado");
    }
}
