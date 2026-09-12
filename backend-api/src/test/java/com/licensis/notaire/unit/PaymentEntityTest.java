package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoPayment;
import com.licensis.notaire.dto.DtoBudget;
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
            payment.setAmount(5000.00f);

            assertThat(payment.getAmount()).isEqualTo(5000.00f);
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
            Payment payment = new Payment(5, 1500.0f, date);

            assertThat(payment.getIdPayment()).isEqualTo(5);
            assertThat(payment.getAmount()).isEqualTo(1500.0f);
            assertThat(payment.getDate()).isEqualTo(date);
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

        @Test
        @DisplayName("getDto with null notes — covers null branch")
        void getDtoWithNullNotes() {
            Payment payment = new Payment(1);
            payment.setDate(new Date());
            payment.setAmount(1000.0f);

            var dto = payment.getDto();

            assertThat(dto.getIdPayment()).isEqualTo(1);
            assertThat(dto.getAmount()).isEqualTo(1000.0f);
        }

        @Test
        @DisplayName("getDto with non-null notes — covers non-null branch")
        void getDtoWithNonNullNotes() {
            Payment payment = new Payment(2);
            payment.setDate(new Date());
            payment.setAmount(500.0f);
            payment.setNotes("Pago parcial");

            var dto = payment.getDto();

            assertThat(dto.getNotes()).isEqualTo("Pago parcial");
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        @Test
        @DisplayName("setAtributos with null notes and null budget — covers null branches")
        void setAtributosAllNullOptional() {
            DtoPayment dto = new DtoPayment();
            dto.setIdPayment(3);
            dto.setDate(new Date());
            dto.setAmount(2000.0f);

            Payment payment = new Payment();
            payment.setAtributos(dto);

            assertThat(payment.getIdPayment()).isEqualTo(3);
            assertThat(payment.getAmount()).isEqualTo(2000.0f);
        }

        @Test
        @DisplayName("setAtributos with non-null notes — covers non-null notes branch")
        void setAtributosWithNotes() {
            DtoPayment dto = new DtoPayment();
            dto.setIdPayment(4);
            dto.setDate(new Date());
            dto.setAmount(300.0f);
            dto.setNotes("Cuota 1");

            Payment payment = new Payment();
            payment.setAtributos(dto);

            assertThat(payment.getNotes()).isEqualTo("Cuota 1");
        }

        @Test
        @DisplayName("setAtributos with non-null budget — covers non-null budget branch")
        void setAtributosWithBudget() {
            DtoPayment dto = new DtoPayment();
            dto.setIdPayment(5);
            dto.setDate(new Date());
            dto.setAmount(750.0f);
            DtoBudget dtoBudget = new DtoBudget();
            dtoBudget.setIdBudget(10);
            dtoBudget.setNumber(1001);
            dtoBudget.setVersion(0);
            dto.setBudget(dtoBudget);

            Payment payment = new Payment();
            payment.setAtributos(dto);

            assertThat(payment.getIdPayment()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("toString and equality edge cases")
    class ToStringAndEqualityTests {

        @Test
        @DisplayName("toString with null notes")
        void toStringWithNullNotes() {
            Payment payment = new Payment(1);
            payment.setDate(new Date());
            payment.setAmount(100.0f);

            String str = payment.toString();

            assertThat(str).contains("1").contains("100");
        }

        @Test
        @DisplayName("toString with non-null notes")
        void toStringWithNonNullNotes() {
            Payment payment = new Payment(2);
            payment.setDate(new Date());
            payment.setAmount(200.0f);
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
