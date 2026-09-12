package com.licensis.notaire.unit;

import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU51", "CU52", "CU53"})
@DisplayName("Testimonio Entity Tests")
class TestimonyEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor and empty lists")
        void shouldInitializeWithDefaultConstructor() {
            Testimony testimony = new Testimony();

            assertThat(testimony.getTestimonyMovementList()).isNotNull().isEmpty();
            assertThat(testimony.getCopyList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Testimony testimony = new Testimony(15);

            assertThat(testimony.getIdTestimony()).isEqualTo(15);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Testimony testimony = new Testimony(3, 101, true);

            assertThat(testimony.getIdTestimony()).isEqualTo(3);
            assertThat(testimony.getNumber()).isEqualTo(101);
            assertThat(testimony.getFlagged()).isTrue();
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get number")
        void shouldSetAndGetNumber() {
            Testimony testimony = new Testimony();
            testimony.setNumber(200);

            assertThat(testimony.getNumber()).isEqualTo(200);
        }

        @Test
        @DisplayName("Should set and get flagged flag")
        void shouldSetAndGetFlagged() {
            Testimony testimony = new Testimony();
            testimony.setFlagged(true);

            assertThat(testimony.getFlagged()).isTrue();

            testimony.setFlagged(false);
            assertThat(testimony.getFlagged()).isFalse();
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            Testimony testimony = new Testimony();
            testimony.setNotes("Requiere notificación al cliente");

            assertThat(testimony.getNotes()).isEqualTo("Requiere notificación al cliente");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Testimony testimony = new Testimony();
            testimony.setVersion(2);

            assertThat(testimony.getVersion()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should set and get deed")
        void shouldSetAndGetDeed() {
            Deed deed = new Deed(1);
            Testimony testimony = new Testimony();
            testimony.setFkIdDeed(deed);

            assertThat(testimony.getFkIdDeed()).isNotNull();
            assertThat(testimony.getFkIdDeed().getIdDeed()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should set and get copia list")
        void shouldSetAndGetCopyList() {
            List<Copy> copies = new ArrayList<>();
            copies.add(new Copy());

            Testimony testimony = new Testimony();
            testimony.setCopyList(copies);

            assertThat(testimony.getCopyList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get movimiento list")
        void shouldSetAndGetMovementList() {
            List<TestimonyMovement> movimientos = new ArrayList<>();
            movimientos.add(new TestimonyMovement());

            Testimony testimony = new Testimony();
            testimony.setTestimonyMovementList(movimientos);

            assertThat(testimony.getTestimonyMovementList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Testimony t1 = new Testimony(5);
            Testimony t2 = new Testimony(5);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Testimony t1 = new Testimony(5);
            Testimony t2 = new Testimony(6);

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            Testimony t = new Testimony(1);

            assertThat(t).isNotEqualTo(null);
            assertThat(t).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Testimony testimony = new Testimony();

            assertThat(testimony.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("toString should include ID and number")
        void toStringShouldIncludeIdAndNumber() {
            Testimony testimony = new Testimony(33, 101, false);
            testimony.setFkIdDeed(new Deed(5));

            assertThat(testimony.toString()).contains("33");
            assertThat(testimony.toString()).contains("101");
        }

        @Test
        @DisplayName("toString should not throw when deed is null")
        void toStringShouldNotThrowWhenDeedIsNull() {
            Testimony testimony = new Testimony(7, 200, false);

            assertThat(testimony.toString()).contains("7").contains("200").contains("null");
        }
    }
}
