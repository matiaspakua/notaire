package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Escritura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU05", "CU06", "CU52", "CU62"})
@DisplayName("Escritura Entity Tests")
class EscrituraEntityTest {

    @Nested
    @DisplayName("CU05 - Preparar escritura - Unit Tests")
    class PrepararEscrituraTests {

        @Test
        @DisplayName("Should create escritura with required fields")
        void shouldCreateEscrituraWithRequiredFields() {
            Escritura escritura = new Escritura();
            escritura.setIdEscritura(1);
            escritura.setNumero(1001);
            escritura.setFechaEscrituracion(new Date());
            escritura.setCuerpo("Escritura de compraventa");
            escritura.setEstado("firmada");

            assertThat(escritura.getNumero()).isEqualTo(1001);
            assertThat(escritura.getCuerpo()).isEqualTo("Escritura de compraventa");
            assertThat(escritura.getEstado()).isEqualTo("firmada");
        }

        @Test
        @DisplayName("Should set escritura states")
        void shouldSetEscrituraStates() {
            Escritura escritura = new Escritura();
            
            escritura.setEstado("firmada");
            assertThat(escritura.getEstado()).isEqualTo("firmada");
            
            escritura.setEstado("no_firmada");
            assertThat(escritura.getEstado()).isEqualTo("no_firmada");
            
            escritura.setEstado("anulada");
            assertThat(escritura.getEstado()).isEqualTo("anulada");
            
            escritura.setEstado("no_paso");
            assertThat(escritura.getEstado()).isEqualTo("no_paso");
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Escritura e1 = new Escritura(1);
            Escritura e2 = new Escritura(1);
            Escritura e3 = new Escritura(2);

            assertThat(e1).isEqualTo(e2);
            assertThat(e1).isNotEqualTo(e3);
        }
    }
}
