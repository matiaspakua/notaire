package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.domain.payment.PaymentDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the payment read and delete use cases (CU15 / CU47).
 */
@DisplayName("PaymentQueryService and DeletePaymentService")
class PaymentQueryAndDeleteServiceTest {

    private InMemoryPaymentRepository payments;
    private PaymentQueryService queries;
    private DeletePaymentService deletePayment;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRepository();
        queries = new PaymentQueryService(payments);
        deletePayment = new DeletePaymentService(payments);
    }

    private static Date at(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private PaymentDetails givenPayment(Integer budgetId, float amount, Date date) {
        return payments.given(new PaymentDetails(null, budgetId, amount, date, null, null));
    }

    @Test
    @DisplayName("Should return every registered payment")
    void shouldFindAll() {
        givenPayment(10, 100f, at(2026, 1, 10));
        givenPayment(11, 200f, at(2026, 2, 10));

        assertThat(queries.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Should return an empty list when nothing is registered")
    void shouldReturnEmptyListWhenNoPayments() {
        assertThat(queries.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should find a payment by its identifier")
    void shouldFindById() {
        PaymentDetails created = givenPayment(10, 100f, at(2026, 1, 10));

        assertThat(queries.findById(created.id())).contains(created);
    }

    @Test
    @DisplayName("Should return empty when the payment does not exist")
    void shouldReturnEmptyWhenNotFound() {
        assertThat(queries.findById(999)).isEmpty();
    }

    @Test
    @DisplayName("Should return only the payments of the requested budget")
    void shouldFindByBudget() {
        givenPayment(10, 100f, at(2026, 1, 10));
        givenPayment(11, 200f, at(2026, 1, 10));

        assertThat(queries.findByBudget(10))
                .singleElement()
                .extracting(PaymentDetails::amount)
                .isEqualTo(100f);
    }

    @Test
    @DisplayName("Should return only the payments inside the requested date range")
    void shouldFindByDateRange() {
        givenPayment(10, 100f, at(2026, 1, 10));
        givenPayment(10, 200f, at(2026, 6, 10));

        assertThat(queries.findByDateRange(at(2026, 1, 1), at(2026, 3, 1)))
                .singleElement()
                .extracting(PaymentDetails::amount)
                .isEqualTo(100f);
    }

    @Test
    @DisplayName("Should delete an existing payment")
    void shouldDeletePayment() {
        PaymentDetails created = givenPayment(10, 100f, at(2026, 1, 10));

        deletePayment.delete(created.id());

        assertThat(queries.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should reject deleting a payment that does not exist")
    void shouldRejectDeletingUnknownPayment() {
        assertThatThrownBy(() -> deletePayment.delete(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pago no encontrado con ID: 999");
    }
}
