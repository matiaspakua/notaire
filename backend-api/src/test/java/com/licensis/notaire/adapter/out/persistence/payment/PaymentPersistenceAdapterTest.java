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
@DisplayName("PaymentService Unit Tests")
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
        testBudget.setPropertyAmount(new java.math.BigDecimal("5000"));

        testPayment = new Payment();
        testPayment.setIdPayment(1);
        testPayment.setAmount(new java.math.BigDecimal("1000"));
        testPayment.setDate(testDate);
        testPayment.setBudget(testBudget);
    }

    @Test
    @DisplayName("Should process a valid payment")
    void shouldProcessValidPayment() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        PaymentDetails result = processPayment.process(
                new ProcessPaymentCommand(1, new java.math.BigDecimal("1000"), testDate, "Test", null));

        assertThat(result).isNotNull()
                .extracting(PaymentDetails::amount, PaymentDetails::id)
                .containsExactly(new java.math.BigDecimal("1000.00"), 1);

        verify(budgetRepository, times(2)).findById(1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(999, new java.math.BigDecimal("1000"), testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget not found");

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
                .hasMessageContaining("The payment amount must be greater than zero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(1, new java.math.BigDecimal("0"), testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The payment amount must be greater than zero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        assertThatThrownBy(() -> processPayment.process(
                new ProcessPaymentCommand(1, new java.math.BigDecimal("-100"), testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The payment amount must be greater than zero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should look up a payment by id")
    void shouldConsultPaymentById() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));

        Optional<PaymentDetails> result = paymentQueries.findById(1);

        assertThat(result).isPresent().get()
                .extracting(PaymentDetails::id, PaymentDetails::budgetId, PaymentDetails::amount)
                .containsExactly(1, 1, new java.math.BigDecimal("1000.00"));

        verify(paymentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty when the payment is not found")
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
    @DisplayName("Should calculate the pending balance correctly")
    void shouldCalculatePendingBalance() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(new java.math.BigDecimal("1000"));

        java.math.BigDecimal result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualByComparingTo(new java.math.BigDecimal("4000"));

        verify(budgetRepository, times(1)).findById(1);
        verify(paymentRepository, times(1)).sumAmountByBudgetId(1);
    }

    @Test
    @DisplayName("Should calculate the pending balance with a null total paid")
    void shouldCalculatePendingBalanceWithNullTotal() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        java.math.BigDecimal result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualByComparingTo(new java.math.BigDecimal("5000"));

        verify(budgetRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should report status NoPayments when the budget has no payments")
    void shouldCalculateStatusWithNoPayments() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        PaymentStatus result = paymentStatus.status(1);

        assertThat(result).isEqualTo(PaymentStatus.NoPayments);
    }

    @Test
    @DisplayName("Should report status PARTIAL when the pending balance is positive but some payments exist")
    void shouldCalculatePartialPaymentStatus() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(new java.math.BigDecimal("1000"));

        PaymentStatus result = paymentStatus.status(1);

        assertThat(result).isEqualTo(PaymentStatus.PARTIAL);
    }

    @Test
    @DisplayName("Should report status PAID when the pending balance is zero")
    void shouldCalculateStatusPaymentSaldado() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(new java.math.BigDecimal("5000"));

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
    @DisplayName("Should delete the payment")
    void shouldDeletePayment() {
        when(paymentRepository.existsById(1)).thenReturn(true);

        deletePayment.delete(1);

        verify(paymentRepository, times(1)).existsById(1);
        verify(paymentRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting a payment that does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        when(paymentRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> deletePayment.delete(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");

        verify(paymentRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("Should edit the payment")
    void shouldEditarPayment() {
        Payment editedPayment = new Payment();
        editedPayment.setIdPayment(1);
        editedPayment.setAmount(new java.math.BigDecimal("2000"));
        editedPayment.setDate(testDate);

        when(paymentRepository.existsById(1)).thenReturn(true);
        when(paymentRepository.findById(1)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(editedPayment);

        PaymentDetails result = editPayment.edit(new EditPaymentCommand(1, new java.math.BigDecimal("2000"), testDate, "Edited", null));

        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(new java.math.BigDecimal("2000"));

        verify(paymentRepository, times(1)).findById(1);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when editing a payment that does not exist")
    void shouldThrowExceptionWhenEditingNonExistentPayment() {
        when(paymentRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(999, new java.math.BigDecimal("1000"), testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment not found");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when editing a payment with an invalid amount")
    void shouldThrowExceptionWhenEditingWithInvalidAmount() {
        when(paymentRepository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(1, new java.math.BigDecimal("-100"), testDate, "Test", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The payment amount must be greater than zero");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should include the submitted-document cost in the budget total (Issue #823)")
    void shouldIncludeDocumentCostInBudgetTotal() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts(new java.math.BigDecimal("1500"))));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        java.math.BigDecimal result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualByComparingTo(new java.math.BigDecimal("6500"));
    }

    @Test
    @DisplayName("Should sum multiple submitted-document costs in the budget total (Issue #823)")
    void shouldSumMultipleDocumentCostsInBudgetTotal() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts(new java.math.BigDecimal("1000"), new java.math.BigDecimal("500"))));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        java.math.BigDecimal result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualByComparingTo(new java.math.BigDecimal("6500"));
    }

    @Test
    @DisplayName("Should not change total when no documents have cost (Issue #823)")
    void shouldNotChangeTotalWhenNoDocumentsHaveCost() {
        testBudget.setProcedureList(List.of(procedureWithDocumentCosts()));

        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));
        when(paymentRepository.sumAmountByBudgetId(1)).thenReturn(null);

        java.math.BigDecimal result = paymentStatus.pendingBalance(1);

        assertThat(result).isEqualByComparingTo(new java.math.BigDecimal("5000"));
    }

    private Procedure procedureWithDocumentCosts(java.math.BigDecimal... importesAPay) {
        List<SubmittedDocument> documents = new ArrayList<>();
        for (java.math.BigDecimal amountToPay : importesAPay) {
            SubmittedDocument document = new SubmittedDocument();
            document.setAmountToPay(amountToPay);
            documents.add(document);
        }

        Procedure procedure = new Procedure();
        procedure.setSubmittedDocumentList(documents);
        return procedure;
    }
}
