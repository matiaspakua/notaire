package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU51", "CU52", "CU53"})
@DisplayName("Testimonio Entity Tests")
class TestimonioEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor and empty lists")
        void shouldInitializeWithDefaultConstructor() {
            Testimonio testimonio = new Testimonio();

            assertThat(testimonio.getMovimientoTestimonioList()).isNotNull().isEmpty();
            assertThat(testimonio.getCopiaList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Testimonio testimonio = new Testimonio(15);

            assertThat(testimonio.getIdTestimonio()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Testimonio testimonio = new Testimonio(3, 101, true);

            assertThat(testimonio.getIdTestimonio()).isEqualTo(3);
            assertThat(testimonio.getNumero()).isEqualTo(101);
            assertThat(testimonio.getObservado()).isTrue();
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get numero")
        void shouldSetAndGetNumero() {
            Testimonio testimonio = new Testimonio();
            testimonio.setNumero(200);

            assertThat(testimonio.getNumero()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should set and get observado flag")
        void shouldSetAndGetObservado() {
            Testimonio testimonio = new Testimonio();
            testimonio.setObservado(true);

            assertThat(testimonio.getObservado()).isTrue();

            testimonio.setObservado(false);
            assertThat(testimonio.getObservado()).isFalse();
        }

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            Testimonio testimonio = new Testimonio();
            testimonio.setObservaciones("Requiere notificación al cliente");

            assertThat(testimonio.getObservaciones()).isEqualTo("Requiere notificación al cliente");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Testimonio testimonio = new Testimonio();
            testimonio.setVersion(2);

            assertThat(testimonio.getVersion()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should set and get escritura")
        void shouldSetAndGetEscritura() {
            Escritura escritura = new Escritura(1);
            Testimonio testimonio = new Testimonio();
            testimonio.setFkIdEscritura(escritura);

            assertThat(testimonio.getFkIdEscritura()).isNotNull();
            assertThat(testimonio.getFkIdEscritura().getIdEscritura()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should set and get copia list")
        void shouldSetAndGetCopiaList() {
            List<Copia> copias = new ArrayList<>();
            copias.add(new Copia());

            Testimonio testimonio = new Testimonio();
            testimonio.setCopiaList(copias);

            assertThat(testimonio.getCopiaList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get movimiento list")
        void shouldSetAndGetMovimientoList() {
            List<MovimientoTestimonio> movimientos = new ArrayList<>();
            movimientos.add(new MovimientoTestimonio());

            Testimonio testimonio = new Testimonio();
            testimonio.setMovimientoTestimonioList(movimientos);

            assertThat(testimonio.getMovimientoTestimonioList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Testimonio t1 = new Testimonio(5);
            Testimonio t2 = new Testimonio(5);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Testimonio t1 = new Testimonio(5);
            Testimonio t2 = new Testimonio(6);

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            Testimonio t = new Testimonio(1);

            assertThat(t).isNotEqualTo(null);
            assertThat(t).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Testimonio testimonio = new Testimonio();

            assertThat(testimonio.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString should include ID and numero")
        void toStringShouldIncludeIdAndNumero() {
            Testimonio testimonio = new Testimonio(33, 101, false);
            testimonio.setFkIdEscritura(new Escritura(5));

            assertThat(testimonio.toString()).contains("33");
            assertThat(testimonio.toString()).contains("101");
        }
    }
}
