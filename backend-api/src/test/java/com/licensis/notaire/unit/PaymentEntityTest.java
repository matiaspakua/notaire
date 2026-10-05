package com.licensis.notaire.unit;


import com.licensis.notaire.business.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU15", "CU47"})
@DisplayName("Pago Entity Tests")
class PaymentEntityTest {

    @Nested
    @DisplayName("CU15 - Procesar pago - Unit Tests")
    class ProcesarPaymentTests {

        @Test
        @DisplayName("Should create pago with required fields")
        void shouldCreatePaymentWithRequiredFields() {
            Payment payment = new Payment();
            payment.setIdPayment(1);
            payment.setDate(new Date());
            payment.setAmount(new java.math.BigDecimal("5000.00"));

            assertThat(payment.getAmount()).isEqualByComparingTo(new java.math.BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Payment p1 = new Payment(1);
            Payment p2 = new Payment(1);
            Payment p3 = new Payment(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Date date = new Date();
            Payment payment = new Payment(5, new java.math.BigDecimal("1500.0"), date);

            assertThat(payment.getIdPayment()).isEqualTo(5);
            assertThat(payment.getAmount()).isEqualByComparingTo(new java.math.BigDecimal("1500.0"));
            assertThat(payment.getDate()).isEqualTo(date);
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

    }

    @Nested
    @DisplayName("toString and equality edge cases")
    class ToStringAndEqualityTests {

        @Test
        @DisplayName("toString with null notes")
        void toStringWithNullNotes() {
            Payment payment = new Payment(1);
            payment.setDate(new Date());
            payment.setAmount(new java.math.BigDecimal("100.0"));

            String str = payment.toString();

            assertThat(str).contains("1").contains("100");
        }

        @Test
        @DisplayName("toString with non-null notes")
        void toStringWithNonNullNotes() {
            Payment payment = new Payment(2);
            payment.setDate(new Date());
            payment.setAmount(new java.math.BigDecimal("200.0"));
            payment.setNotes("Observación de prueba");

            String str = payment.toString();

            assertThat(str).contains("Observación de prueba");
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Payment(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Payment(1)).isNotEqualTo("not a pago");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Payment().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Payment()).isNotEqualTo(new Payment(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Payment()).isEqualTo(new Payment());
        }
    }
}
