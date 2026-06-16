package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU42", "CU50", "CU56"})
@DisplayName("DocumentoPresentado Entity Tests")
class DocumentoPresentadoEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            DocumentoPresentado doc = new DocumentoPresentado();
            assertThat(doc).isNotNull();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            DocumentoPresentado doc = new DocumentoPresentado(42);
            assertThat(doc.getIdDocumentoPresentado()).isEqualTo(42);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            DocumentoPresentado doc = new DocumentoPresentado(10, "Doc test", null, false, false, false, false);
            assertThat(doc.getIdDocumentoPresentado()).isEqualTo(10);
            assertThat(doc.getNombre()).isEqualTo("Doc test");
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get nombre")
        void shouldSetAndGetNombre() {
            DocumentoPresentado doc = new DocumentoPresentado();
            doc.setNombre("Escritura de compraventa");
            assertThat(doc.getNombre()).isEqualTo("Escritura de compraventa");
        }

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            DocumentoPresentado doc = new DocumentoPresentado();
            doc.setObservaciones("Requiere notificación al registro");
            assertThat(doc.getObservaciones()).isEqualTo("Requiere notificación al registro");
        }

        @Test
        @DisplayName("Should set and get observado")
        void shouldSetAndGetObservado() {
            DocumentoPresentado doc = new DocumentoPresentado();
            doc.setObservado(Boolean.TRUE);
            assertThat(doc.getObservado()).isTrue();
            doc.setObservado(Boolean.FALSE);
            assertThat(doc.getObservado()).isFalse();
        }

        @Test
        @DisplayName("Should set and get liberado")
        void shouldSetAndGetLiberado() {
            DocumentoPresentado doc = new DocumentoPresentado();
            doc.setLiberado(Boolean.TRUE);
            assertThat(doc.getLiberado()).isTrue();
        }

        @Test
        @DisplayName("Should set and get entregado (nullable Boolean)")
        void shouldSetAndGetEntregado() {
            DocumentoPresentado doc = new DocumentoPresentado();
            assertThat(doc.getEntregado()).isNull();
            doc.setEntregado(Boolean.TRUE);
            assertThat(doc.getEntregado()).isTrue();
            doc.setEntregado(Boolean.FALSE);
            assertThat(doc.getEntregado()).isFalse();
        }

        @Test
        @DisplayName("Should set and get reingresado (nullable Boolean)")
        void shouldSetAndGetReingresado() {
            DocumentoPresentado doc = new DocumentoPresentado();
            assertThat(doc.getReingresado()).isNull();
            doc.setReingresado(Boolean.TRUE);
            assertThat(doc.getReingresado()).isTrue();
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            DocumentoPresentado doc = new DocumentoPresentado();
            doc.setVersion(3);
            assertThat(doc.getVersion()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            DocumentoPresentado d1 = new DocumentoPresentado(5);
            DocumentoPresentado d2 = new DocumentoPresentado(5);
            assertThat(d1).isEqualTo(d2);
            assertThat(d1.hashCode()).isEqualTo(d2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            DocumentoPresentado d1 = new DocumentoPresentado(5);
            DocumentoPresentado d2 = new DocumentoPresentado(6);
            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            DocumentoPresentado d = new DocumentoPresentado(1);
            assertThat(d).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            DocumentoPresentado d = new DocumentoPresentado(1);
            assertThat(d).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            DocumentoPresentado d = new DocumentoPresentado();
            assertThat(d.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode should be non-zero when ID is set")
        void hashCodeShouldBeNonZeroWhenIdSet() {
            DocumentoPresentado d = new DocumentoPresentado(99);
            assertThat(d.hashCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("Should be equal when both IDs are null")
        void shouldBeEqualWhenBothIdsAreNull() {
            DocumentoPresentado d1 = new DocumentoPresentado();
            DocumentoPresentado d2 = new DocumentoPresentado();
            assertThat(d1).isEqualTo(d2);
        }

        @Test
        @DisplayName("Should not be equal when this ID is null and other has ID")
        void shouldNotBeEqualWhenThisIdNullOtherHasId() {
            DocumentoPresentado d1 = new DocumentoPresentado();
            DocumentoPresentado d2 = new DocumentoPresentado(5);
            assertThat(d1).isNotEqualTo(d2);
        }

        @Test
        @DisplayName("toString should contain key fields")
        void toStringShouldContainKeyFields() {
            DocumentoPresentado doc = new DocumentoPresentado(7);
            doc.setNumeroCarton(101);
            String str = doc.toString();
            assertThat(str).contains("7");
        }
    }
}
