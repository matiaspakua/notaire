package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoPago;
import com.licensis.notaire.dto.DtoPresupuesto;
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

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Date fecha = new Date();
            Pago pago = new Pago(5, 1500.0f, fecha);

            assertThat(pago.getIdPago()).isEqualTo(5);
            assertThat(pago.getMonto()).isEqualTo(1500.0f);
            assertThat(pago.getFecha()).isEqualTo(fecha);
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

        @Test
        @DisplayName("getDto with null observaciones — covers null branch")
        void getDtoWithNullObservaciones() {
            Pago pago = new Pago(1);
            pago.setFecha(new Date());
            pago.setMonto(1000.0f);

            var dto = pago.getDto();

            assertThat(dto.getIdPago()).isEqualTo(1);
            assertThat(dto.getMonto()).isEqualTo(1000.0f);
        }

        @Test
        @DisplayName("getDto with non-null observaciones — covers non-null branch")
        void getDtoWithNonNullObservaciones() {
            Pago pago = new Pago(2);
            pago.setFecha(new Date());
            pago.setMonto(500.0f);
            pago.setObservaciones("Pago parcial");

            var dto = pago.getDto();

            assertThat(dto.getObservaciones()).isEqualTo("Pago parcial");
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        @Test
        @DisplayName("setAtributos with null observaciones and null presupuesto — covers null branches")
        void setAtributosAllNullOptional() {
            DtoPago dto = new DtoPago();
            dto.setIdPago(3);
            dto.setFecha(new Date());
            dto.setMonto(2000.0f);

            Pago pago = new Pago();
            pago.setAtributos(dto);

            assertThat(pago.getIdPago()).isEqualTo(3);
            assertThat(pago.getMonto()).isEqualTo(2000.0f);
        }

        @Test
        @DisplayName("setAtributos with non-null observaciones — covers non-null observaciones branch")
        void setAtributosWithObservaciones() {
            DtoPago dto = new DtoPago();
            dto.setIdPago(4);
            dto.setFecha(new Date());
            dto.setMonto(300.0f);
            dto.setObservaciones("Cuota 1");

            Pago pago = new Pago();
            pago.setAtributos(dto);

            assertThat(pago.getObservaciones()).isEqualTo("Cuota 1");
        }

        @Test
        @DisplayName("setAtributos with non-null presupuesto — covers non-null presupuesto branch")
        void setAtributosWithPresupuesto() {
            DtoPago dto = new DtoPago();
            dto.setIdPago(5);
            dto.setFecha(new Date());
            dto.setMonto(750.0f);
            DtoPresupuesto dtoPresupuesto = new DtoPresupuesto();
            dtoPresupuesto.setIdPresupuesto(10);
            dtoPresupuesto.setNumero(1001);
            dtoPresupuesto.setVersion(0);
            dto.setPresupuesto(dtoPresupuesto);

            Pago pago = new Pago();
            pago.setAtributos(dto);

            assertThat(pago.getIdPago()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("toString and equality edge cases")
    class ToStringAndEqualityTests {

        @Test
        @DisplayName("toString with null observaciones")
        void toStringWithNullObservaciones() {
            Pago pago = new Pago(1);
            pago.setFecha(new Date());
            pago.setMonto(100.0f);

            String str = pago.toString();

            assertThat(str).contains("1").contains("100");
        }

        @Test
        @DisplayName("toString with non-null observaciones")
        void toStringWithNonNullObservaciones() {
            Pago pago = new Pago(2);
            pago.setFecha(new Date());
            pago.setMonto(200.0f);
            pago.setObservaciones("Observación de prueba");

            String str = pago.toString();

            assertThat(str).contains("Observación de prueba");
        }

        @Test
        @DisplayName("not equal to null")
        void notEqualToNull() {
            assertThat(new Pago(1)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("not equal to different type")
        void notEqualToDifferentType() {
            assertThat(new Pago(1)).isNotEqualTo("not a pago");
        }

        @Test
        @DisplayName("hashCode is zero when id is null")
        void hashCodeZeroWhenIdNull() {
            assertThat(new Pago().hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("null id is not equal to non-null id")
        void nullIdNotEqualToNonNull() {
            assertThat(new Pago()).isNotEqualTo(new Pago(1));
        }

        @Test
        @DisplayName("both null ids are equal")
        void bothNullIdsAreEqual() {
            assertThat(new Pago()).isEqualTo(new Pago());
        }
    }
}
