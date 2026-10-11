package com.licensis.notaire.integration;


import com.licensis.notaire.application.port.in.payment.DeletePaymentUseCase;
import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.application.port.in.payment.EditPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.GetPaymentStatusUseCase;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentCommand;
import com.licensis.notaire.application.port.in.payment.ProcessPaymentUseCase;
import com.licensis.notaire.application.port.in.payment.QueryPaymentsUseCase;
import com.licensis.notaire.domain.payment.PaymentDetails;
import com.licensis.notaire.exception.PendingBalanceExceededException;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end check of the CU15/CU47 payment slice: the use cases are exercised as
 * Spring beans, so the real JPA outbound adapters and a real transaction boundary
 * take part. Formerly {@code PaymentServiceIntegrationTest}; expectations unchanged.
 */
@DisplayName("PaymentService Integration Tests")
class PaymentUseCaseIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private ProcessPaymentUseCase processPayment;

    @Autowired
    private EditPaymentUseCase editPayment;

    @Autowired
    private DeletePaymentUseCase deletePayment;

    @Autowired
    private QueryPaymentsUseCase paymentQueries;

    @Autowired
    private GetPaymentStatusUseCase paymentStatus;

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
        testPerson.setFirstName("Client");
        testPerson.setLastName("Test");
        testPerson.setIdentificationNumber("12345678");
        testPerson.setIsClient(true);
        testPerson.setFkIdIdentificationType(identificationType);
        testPerson = personRepository.save(testPerson);

        testBudget = new Budget();
        testBudget.setNumber((int) (System.currentTimeMillis() % 10000));
        testBudget.setDate(new Date());
        testBudget.setHeading("Budget Test");
        testBudget.setStatus("PENDIENTE");
        testBudget.setPropertyAmount(new java.math.BigDecimal("500000"));
        testBudget.setFkIdPerson(testPerson);
        testBudget = budgetRepository.save(testBudget);
    }

    private PaymentDetails process(Integer budgetId, java.math.BigDecimal amount, Date date, String notes) {
        return processPayment.process(new ProcessPaymentCommand(budgetId, amount, date, notes, null));
    }

    private PaymentDetails edit(Integer paymentId, java.math.BigDecimal amount, Date date, String notes) {
        return editPayment.edit(new EditPaymentCommand(paymentId, amount, date, notes, null));
    }

    @Test
    @DisplayName("Should process a valid payment through service")
    void shouldProcessValidPaymentThroughService() {
        PaymentDetails result = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "First payment");

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(new java.math.BigDecimal("100000"));
    }

    @Test
    @DisplayName("Should throw exception when budget not found")
    void shouldThrowExceptionWhenBudgetNotFound() {
        assertThatThrownBy(() -> process(9999, new java.math.BigDecimal("100000"), new Date(), "Test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should calculate the pending balance correctly")
    void shouldCalculatePendingBalanceCorrectly() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment 1");

        java.math.BigDecimal pendingBalance = paymentStatus.pendingBalance(testBudget.getIdBudget());

        assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("400000"));
    }

    @Test
    @DisplayName("Should calculate the pending balance with multiple payments")
    void shouldCalculatePendingBalanceWithMultiplePayments() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment 1");
        process(testBudget.getIdBudget(), new java.math.BigDecimal("150000"), new Date(), "Payment 2");

        java.math.BigDecimal pendingBalance = paymentStatus.pendingBalance(testBudget.getIdBudget());

        assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("250000"));
    }

    @Test
    @DisplayName("Should reject a payment that exceeds the outstanding balance and not persist it")
    void shouldRejectPaymentExceedingPendingBalance() {
        assertThatThrownBy(() -> process(testBudget.getIdBudget(), new java.math.BigDecimal("600000"), new Date(), "Overpay attempt"))
                .isInstanceOf(PendingBalanceExceededException.class);

        List<PaymentDetails> payments = paymentQueries.findByBudget(testBudget.getIdBudget());
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should reject a payment that exceeds the balance already reduced by a prior payment")
    void shouldRejectPaymentExceedingBalanceReducedByPriorPayment() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("400000"), new Date(), "Payment 1");

        assertThatThrownBy(() ->
                process(testBudget.getIdBudget(), new java.math.BigDecimal("150000"), new Date(), "Overpay against reduced balance"))
                .isInstanceOf(PendingBalanceExceededException.class);

        List<PaymentDetails> payments = paymentQueries.findByBudget(testBudget.getIdBudget());
        assertThat(payments).hasSize(1);
    }

    @Test
    @DisplayName("Should find payments by budget through service")
    void shouldFindPaymentsByBudgetThroughService() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment 1");

        List<PaymentDetails> found = paymentQueries.findByBudget(testBudget.getIdBudget());

        assertThat(found).isNotEmpty()
                .hasSize(1)
                .allMatch(p -> p.budgetId().equals(testBudget.getIdBudget()));
    }

    @Test
    @DisplayName("Should find all payments through service")
    void shouldFindAllPaymentsThroughService() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        List<PaymentDetails> all = paymentQueries.findAll();

        assertThat(all).isNotEmpty();
    }

    @Test
    @DisplayName("Should find a payment by id through service")
    void shouldFindPaymentByIdThroughService() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        Optional<PaymentDetails> found = paymentQueries.findById(saved.id());

        assertThat(found).isPresent()
                .hasValueSatisfying(p -> assertThat(p.amount()).isEqualByComparingTo(new java.math.BigDecimal("100000")));
    }

    @Test
    @DisplayName("Should find payments by date range through service")
    void shouldFindPaymentsByDateRangeThroughService() {
        Date now = new Date();
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), now, "Payment");

        Date startDate = new Date(now.getTime() - 86400000);
        Date endDate = new Date(now.getTime() + 86400000);

        List<PaymentDetails> found = paymentQueries.findByDateRange(startDate, endDate);

        assertThat(found).isNotEmpty();
    }

    @Test
    @DisplayName("Should delete the payment through service")
    void shouldDeletePaymentThroughService() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        deletePayment.delete(saved.id());

        Optional<PaymentDetails> deleted = paymentQueries.findById(saved.id());
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should edit the payment through service")
    void shouldEditPaymentThroughService() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        PaymentDetails edited = edit(saved.id(), new java.math.BigDecimal("120000"), new Date(), "Edited");

        assertThat(edited).isNotNull();
        assertThat(edited.amount()).isEqualByComparingTo(new java.math.BigDecimal("120000"));
    }

    @Test
    @DisplayName("Should enforce amount validation in service")
    void shouldEnforcAmountValidationInService() {
        assertThatThrownBy(() -> process(testBudget.getIdBudget(), new java.math.BigDecimal("-100"), new Date(), "Invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should maintain transaction consistency across service methods")
    void shouldMaintainTransactionConsistency() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        deletePayment.delete(saved.id());

        List<PaymentDetails> payments = paymentQueries.findByBudget(testBudget.getIdBudget());
        assertThat(payments).isEmpty();
    }

    @Test
    @DisplayName("Should handle updatePayment with null amount")
    void shouldHandleEditarPaymentWithNullAmount() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment Original");

        PaymentDetails edited = edit(saved.id(), null, new Date(), "Updated");

        assertThat(edited).isNotNull();
        assertThat(edited.amount()).isEqualByComparingTo(new java.math.BigDecimal("100000"));
        assertThat(edited.notes()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Should handle updatePayment with null date")
    void shouldHandleEditarPaymentWithNullDate() {
        Date originalDate = new Date();
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), originalDate, "Payment");

        PaymentDetails edited = edit(saved.id(), new java.math.BigDecimal("120000"), null, "Updated");

        assertThat(edited).isNotNull();
        assertThat(edited.amount()).isEqualByComparingTo(new java.math.BigDecimal("120000"));
        assertThat(edited.date()).isEqualTo(originalDate);
    }

    @Test
    @DisplayName("Should handle updatePayment with null notes")
    void shouldHandleEditarPaymentWithNullNotes() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Original");

        PaymentDetails edited = edit(saved.id(), new java.math.BigDecimal("120000"), new Date(), null);

        assertThat(edited).isNotNull();
        assertThat(edited.amount()).isEqualByComparingTo(new java.math.BigDecimal("120000"));
        assertThat(edited.notes()).isEqualTo("Original");
    }

    @Test
    @DisplayName("Should reject negative amount in updatePayment")
    void shouldRejectNegativeAmountInEditarPayment() {
        PaymentDetails saved = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), new Date(), "Payment");

        assertThatThrownBy(() -> edit(saved.id(), new java.math.BigDecimal("-50000"), new Date(), "Invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Should throw exception when editing a payment that does not exist")
    void shouldThrowExceptionWhenEditingNonExistentPayment() {
        assertThatThrownBy(() -> edit(9999, new java.math.BigDecimal("100000"), new Date(), "Test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw exception when deleting a payment that does not exist")
    void shouldThrowExceptionWhenDeletingNonExistentPayment() {
        assertThatThrownBy(() -> deletePayment.delete(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should handle processPayment with null date and use current date")
    void shouldHandleProcessPaymentWithNullDate() {
        long beforeTime = System.currentTimeMillis();

        PaymentDetails result = process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), null, "Payment");

        long afterTime = System.currentTimeMillis();

        assertThat(result).isNotNull();
        assertThat(result.date()).isNotNull();
        assertThat(result.date().getTime()).isBetween(beforeTime, afterTime);
    }

    @Test
    @DisplayName("Should handle zero amount in processPayment")
    void shouldRejectZeroAmountInProcessarPayment() {
        assertThatThrownBy(() -> process(testBudget.getIdBudget(), new java.math.BigDecimal("0"), new Date(), "Invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Should handle multiple payments with different amounts")
    void shouldHandleMultiplePaymentsWithDifferentAmounts() {
        java.math.BigDecimal amount1 = new java.math.BigDecimal("50000");
        java.math.BigDecimal amount2 = new java.math.BigDecimal("75000");
        java.math.BigDecimal amount3 = new java.math.BigDecimal("125000");

        process(testBudget.getIdBudget(), amount1, new Date(), "Payment 1");
        process(testBudget.getIdBudget(), amount2, new Date(), "Payment 2");
        process(testBudget.getIdBudget(), amount3, new Date(), "Payment 3");

        List<PaymentDetails> payments = paymentQueries.findByBudget(testBudget.getIdBudget());

        assertThat(payments)
                .hasSize(3)
                .extracting(PaymentDetails::amount)
                .usingElementComparator(java.math.BigDecimal::compareTo)
                .containsExactlyInAnyOrder(amount1, amount2, amount3);

        java.math.BigDecimal pendingBalance = paymentStatus.pendingBalance(testBudget.getIdBudget());
        java.math.BigDecimal totalPaid = amount1.add(amount2).add(amount3);
        assertThat(pendingBalance).isEqualByComparingTo(new java.math.BigDecimal("500000").subtract(totalPaid));
    }

    @Test
    @DisplayName("Should find payments by date range with multiple entries")
    void shouldFindPaymentsByDateRangeWithMultipleEntries() {
        long now = System.currentTimeMillis();
        Date date1 = new Date(now - 2 * 86400000);
        Date date2 = new Date(now - 86400000);
        Date date3 = new Date(now);

        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), date1, "Payment 1");
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), date2, "Payment 2");
        process(testBudget.getIdBudget(), new java.math.BigDecimal("100000"), date3, "Payment 3");

        Date startDate = new Date(now - 3 * 86400000);
        Date endDate = new Date(now + 86400000);

        List<PaymentDetails> found = paymentQueries.findByDateRange(startDate, endDate);

        List<PaymentDetails> foundForBudget = found.stream()
                .filter(p -> testBudget.getIdBudget().equals(p.budgetId()))
                .toList();
        assertThat(foundForBudget).hasSize(3);
    }

    @Test
    @DisplayName("Should calculate the balance correctly with a full payment")
    void shouldCalculateBalanceWithFullPayment() {
        process(testBudget.getIdBudget(), new java.math.BigDecimal("500000"), new Date(), "Full payment");

        java.math.BigDecimal pendingBalance = paymentStatus.pendingBalance(testBudget.getIdBudget());
        assertThat(pendingBalance).isZero();
    }

    @Test
    @DisplayName("Should throw an exception when calculating the pending balance for a missing budget")
    void shouldThrowExceptionWhenCalculatingBalanceForMissingBudget() {
        assertThatThrownBy(() -> paymentStatus.pendingBalance(9999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}
