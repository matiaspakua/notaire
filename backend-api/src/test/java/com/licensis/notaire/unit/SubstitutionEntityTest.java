package com.licensis.notaire.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU22", "CU23"})
@DisplayName("Suplencia Entity Tests")
class SubstitutionEntityTest {

    private Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            Substitution substitution = new Substitution();

            assertThat(substitution.getIdSubstitution()).isNull();
            assertThat(substitution.getNotes()).isNull();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Substitution substitution = new Substitution(55);

            assertThat(substitution.getIdSubstitution()).isEqualTo(55);
        }

        @Test
        @DisplayName("Should initialize with ID, dateStart and dateEnd constructor")
        void shouldInitializeWithFullConstructor() {
            Date start = toDate(LocalDate.of(2026, 1, 1));
            Date end = toDate(LocalDate.of(2026, 12, 31));

            Substitution substitution = new Substitution(10, start, end);

            assertThat(substitution.getIdSubstitution()).isEqualTo(10);
            assertThat(substitution.getDateStart()).isEqualTo(start);
            assertThat(substitution.getDateEnd()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get dates")
        void shouldSetAndGetDates() {
            Date start = toDate(LocalDate.of(2026, 3, 1));
            Date end = toDate(LocalDate.of(2026, 3, 31));

            Substitution substitution = new Substitution();
            substitution.setDateStart(start);
            substitution.setDateEnd(end);

            assertThat(substitution.getDateStart()).isEqualTo(start);
            assertThat(substitution.getDateEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            Substitution substitution = new Substitution();
            substitution.setNotes("Licencia por enfermedad");

            assertThat(substitution.getNotes()).isEqualTo("Licencia por enfermedad");
        }

        @Test
        @DisplayName("Should set and get suplente")
        void shouldSetAndGetSuplente() {
            Person suplente = new Person();
            suplente.setFirstName("Carlos");
            suplente.setLastName("Rodriguez");

            Substitution substitution = new Substitution();
            substitution.setFkIdSubstitute(suplente);

            assertThat(substitution.getFkIdSubstitute()).isNotNull();
            assertThat(substitution.getFkIdSubstitute().getFirstName()).isEqualTo("Carlos");
        }

        @Test
        @DisplayName("Should set and get suplantado")
        void shouldSetAndGetSuplantado() {
            Person suplantado = new Person();
            suplantado.setFirstName("Maria");
            suplantado.setLastName("Gomez");

            Substitution substitution = new Substitution();
            substitution.setFkIdSubstituted(suplantado);

            assertThat(substitution.getFkIdSubstituted()).isNotNull();
            assertThat(substitution.getFkIdSubstituted().getFirstName()).isEqualTo("Maria");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Substitution substitution = new Substitution();
            substitution.setVersion(2);

            assertThat(substitution.getVersion()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Substitution s1 = new Substitution(10);
            Substitution s2 = new Substitution(10);

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Substitution s1 = new Substitution(10);
            Substitution s2 = new Substitution(20);

            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Substitution substitution = new Substitution(1);

            assertThat(substitution).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Substitution substitution = new Substitution(1);

            assertThat(substitution).isNotEqualTo("string");
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Substitution substitution = new Substitution(77);

            assertThat(substitution.toString()).contains("77");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Substitution substitution = new Substitution();

            assertThat(substitution.hashCode()).isEqualTo(0);
        }
    }
}
