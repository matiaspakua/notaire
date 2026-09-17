package com.licensis.notaire.adapter.out.persistence.payment;

import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.usecase.payment.DeletePaymentService;
import com.licensis.notaire.application.usecase.payment.EditPaymentService;
import com.licensis.notaire.application.usecase.payment.PaymentQueryService;
import com.licensis.notaire.application.usecase.payment.PaymentStatusService;
import com.licensis.notaire.application.usecase.payment.ProcessPaymentService;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.domain.payment.PaymentStatus;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for the payment slice driven through the outbound JPA adapters
 * over mocked Spring Data repositories (ADR-021).
 *
 * <p>Formerly {@code service.unit.PaymentServiceTest}: the expectations are unchanged,
 * but the system under test is now the use case plus its persistence adapter, so the
 * very same repository interactions are still asserted.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PagoService Unit Tests")
class PaymentPersistenceAdapterTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    private ProcessPaymentService processPayment;
    private EditPaymentService editPayment;
    private DeletePaymentService deletePayment;
    private PaymentQueryService paymentQueries;
    private PaymentStatusService paymentStatus;

    private Budget testBudget;
    private Payment testPayment;
    private Date testDate;

    @BeforeEach
    void setUp() {
        PaymentPersistenceAdapter payments = new PaymentPersistenceAdapter(paymentRepository, budgetRepository);
        BudgetLookupAdapter budgets = new BudgetLookupAdapter(budgetRepository, procedureRepository);

        processPayment = new ProcessPaymentService(payments, budgets);
        editPayment = new EditPaymentService(payments);
        deletePayment = new DeletePaymentService(payments);
        paymentQueries = new PaymentQueryService(payments);
        paymentStatus = new PaymentStatusService(payments, budgets);

        testDate = new Date();

        testBudget = new Budget();
        testBudget.setIdBudget(1);
        testBudget.setNumber(100);
        testBudget.setPropertyAmount(5000f);

        testPayment = new Payment();
        testPayment.setIdPayment(1);
        testPayment.setAmount(1000f);
        testPayment.setDate(testDate);
        testPayment.setBudget(testBudget);
    }

    @Test
    @DisplayName("Should process valid pago")
    void shouldProcessValidPayment() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentDetails result = processPayment.process(
                new ProcessPaymentCommand(1, 1000f, testDate, "Test", null));

        assertThat(result).isNotNull()
                .extracting(PaymentDetails::amount, PaymentDetails::id)
                .containsExactly(1000f, 1);

        verify(budgetRepository, times(2)).findById(1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(999, 1000f, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(budgetRepository, times(1)).findById(999);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is null")
    void shouldThrowExceptionWhenAmountIsNull() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(1, null, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El monto del pago debe ser mayor a cero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(1, 0f, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El monto del pago debe ser mayor a cero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(1, -100f, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El monto del pago debe ser mayor a cero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should consult pago by id")
    void shouldConsultPaymentById() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));

        Optional<PaymentDetails> result = paymentQueries.findById(1);

        assertThat(result).isPresent().get()
                .extracting(PaymentDetails::id, PaymentDetails::budgetId, PaymentDetails::amount)
                .containsExactly(1, 1, 1000f);

        verify(paymentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when pago not found")
    void shouldReturnEmptyWhenPaymentNotFound() {
        when(paymentRepository.findById(999)).thenReturn(Optional.empty());

        Optional<PaymentDetails> result = paymentQueries.findById(999);

        assertThat(result).isEmpty();

        verify(paymentRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find payments by budget")
    void shouldFindPaymentsByBudget() {
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);

        when(paymentRepository.findByFkIdBudgetIdBudget(1)).thenReturn(payments);

        List<PaymentDetails> result = paymentQueries.findByBudget(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .extracting(PaymentDetails::id)
                .containsExactly(1);

        verify(paymentRepository, times(1)).findByFkIdBudgetIdBudget(1);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente correctly")
    void shouldCalculateSaldoPending() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(1000f);

        float result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualTo(4000f);

        verify(budgetRepository, times(1)).findById(1);
        verify(paymentRepository, times(1)).sumAmountByBudgetId(1);
    }

    @Test
    @DisplayName("Should calculate saldo pendiente with null totalPaid")
    void shouldCalculateSaldoPendingWithNullTotal() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        float result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualTo(5000f);

        verify(budgetRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should report status SIN_PAGOS when budget has no payments")
    void shouldCalculateStatusPaymentSinPayments() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        PaymentStatus result = paymentStatus.status(1);

        assertThat(result).isEqualTo(PaymentStatus.NoPayments);
    }

    @Test
    @DisplayName("Should report status PARTIAL when saldo pendiente is positive but some payments exist")
    void shouldCalculateStatusPaymentParcial() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(1000f);

        PaymentStatus result = paymentStatus.status(1);

        assertThat(result).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    @DisplayName("Should report status PAID when saldo pendiente is zero")
    void shouldCalculateStatusPaymentSaldado() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(5000f);

        PaymentStatus result = paymentStatus.status(1);

        assertThat(result).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should find all payments")
    void shouldFindAll() {
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);

        when(paymentRepository.findAll()).thenReturn(payments);

        List<PaymentDetails> result = paymentQueries.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .extracting(PaymentDetails::id)
                .containsExactly(1);

        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find payments by date range")
    void shouldFindPaymentsByDateRange() {
        Date startDate = new Date(testDate.getTime() - 86400000);
        Date endDate = new Date(testDate.getTime() + 86400000);
        List<Payment> payments = new ArrayList<>();
        payments.add(testPayment);

        when(paymentRepository.findByDateBetween(startDate, endDate)).thenReturn(payments);

        List<PaymentDetails> result = paymentQueries.findByDateRange(startDate, endDate);

        assertThat(result).isNotNull()
                .hasSize(1)
                .extracting(PaymentDetails::id)
                .containsExactly(1);

        verify(paymentRepository, times(1)).findByDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("Should delete pago")
    void shouldDeletePayment() {
        when(paymentRepository.existsById(1)).thenReturn(true);

        deletePayment.delete(1);

        verify(paymentRepository, times(1)).existsById(1);
        verify(paymentRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent pago")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        when(paymentRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> deletePayment.delete(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pago no encontrado");

        verify(paymentRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should edit pago")
    void shouldEditarPayment() {
        Payment editedPayment = new Payment();
        editedPayment.setIdPayment(1);
        editedPayment.setAmount(2000f);
        editedPayment.setDate(testDate);

        when(paymentRepository.existsById(1)).thenReturn(true);
        when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(editedPayment);

        PaymentDetails result = editPayment.edit(new EditPaymentCommand(1, 2000f, testDate, "Edited", null));

        assertThat(result).isNotNull()
                .extracting(PaymentDetails::amount)
                .isEqualTo(2000f);

        verify(paymentRepository, times(1)).findById(1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent pago")
    void shouldThrowExceptionWhenEditingNonExistentPayment() {
        when(paymentRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(999, 1000f, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Pago no encontrado");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when editing pago with invalid amount")
    void shouldThrowExceptionWhenEditingWithInvalidAmount() {
        when(paymentRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(1, -100f, testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El monto del pago debe ser mayor a cero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should include documento presentado cost in budget total (Issue #823)")
    void shouldIncludeDocumentCostInBudgetTotal() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts(1500f)));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        float result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualTo(6500f);
    }

    @Test
    @DisplayName("Should sum multiple documento presentado costs in budget total (Issue #823)")
    void shouldSumMultipleDocumentCostsInBudgetTotal() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts(1000f, 500f)));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        float result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualTo(6500f);
    }

    @Test
    @DisplayName("Should not change total when no documents have cost (Issue #823)")
    void shouldNotChangeTotalWhenNoDocumentsHaveCost() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts()));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        float result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualTo(5000f);
    }

    private Procedure procedureWithDocumentCosts(Float... importesAPay) {
        List<SubmittedDocument> documents = new ArrayList<>();
        for (Float amountToPay : importesAPay) {
            SubmittedDocument document = new SubmittedDocument();
            document.setAmountToPay(amountToPay);
            documents.add(document);
        }

        Procedure procedure = new Procedure();
        procedure.setSubmittedDocumentList(documents);
        return procedure;
    }
}
