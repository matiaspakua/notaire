package com.licensis.notaire.application.usecase.payment;

import com.licensis.notaire.application.port.in.payment.EditPaymentCommand;
import com.licensis.notaire.domain.payment.PaymentDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for editing a registered payment (CU15), covering the partial-update
 * semantics where a null field means "leave unchanged".
 */
@DisplayName("EditPaymentService (CU15)")
class EditPaymentServiceTest {

    private InMemoryPaymentRepository payments;
    private EditPaymentService editPayment;
    private PaymentDetails existing;

    @BeforeEach
    void setUp() {
        payments = new InMemoryPaymentRepository();
        editPayment = new EditPaymentService(payments);
        existing = payments.given(new PaymentDetails(1, 10, 500f, new Date(), "EFECTIVO", "Original"));
    }

    @Test
    @DisplayName("Should update the amount when a new one is supplied")
    void shouldUpdateAmount() {
        PaymentDetails updated = editPayment.edit(
                new EditPaymentCommand(existing.id(), 750f, null, null, null));

        assertThat(updated.amount()).isEqualTo(750f);
    }

    @Test
    @DisplayName("Should leave untouched every field left null")
    void shouldLeaveNullFieldsUnchanged() {
        PaymentDetails updated = editPayment.edit(
                new EditPaymentCommand(existing.id(), 750f, null, null, null));

        assertThat(updated.notes()).isEqualTo("Original");
        assertThat(updated.paymentMethod()).isEqualTo("EFECTIVO");
        assertThat(updated.date()).isEqualTo(existing.date());
    }

    @Test
    @DisplayName("Should update notes, date and payment method when supplied")
    void shouldUpdateRemainingFields() {
        Date newDate = new Date(0L);

        PaymentDetails updated = editPayment.edit(
                new EditPaymentCommand(existing.id(), null, newDate, "Corregido", "TRANSFERENCIA"));

        assertThat(updated.notes()).isEqualTo("Corregido");
        assertThat(updated.paymentMethod()).isEqualTo("TRANSFERENCIA");
        assertThat(updated.date()).isEqualTo(newDate);
        assertThat(updated.amount()).isEqualTo(500f);
    }

    @Test
    @DisplayName("Should reject a zero or negative amount")
    void shouldRejectNonPositiveAmount() {
        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(existing.id(), 0f, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto del pago debe ser mayor a cero");

        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(existing.id(), -5f, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El monto del pago debe ser mayor a cero");
    }

    @Test
    @DisplayName("Should reject editing a payment that does not exist")
    void shouldRejectUnknownPayment() {
        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(999, 100f, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pago no encontrado con ID: 999");
    }

    @Test
    @DisplayName("Should reject an unknown payment before validating the amount")
    void shouldCheckExistenceBeforeAmount() {
        assertThatThrownBy(() -> editPayment.edit(new EditPaymentCommand(999, -1f, null, null, null)))
                .hasMessage("Pago no encontrado con ID: 999");
    }
}
