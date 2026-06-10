package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Pago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU15", "CU47"})
@DisplayName("Pago Entity Tests")
class PagoEntityTest {

    @Nested
    @DisplayName("CU15 - Procesar pago - Unit Tests")
    class ProcesarPagoTests {

        @Test
        @DisplayName("Should create pago with required fields")
        void shouldCreatePagoWithRequiredFields() {
            Pago pago = new Pago();
            pago.setIdPago(1);
            pago.setFecha(new Date());
            pago.setMonto(5000.00f);

            assertThat(pago.getMonto()).isEqualTo(5000.00f);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Pago p1 = new Pago(1);
            Pago p2 = new Pago(1);
            Pago p3 = new Pago(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }
    }
}
