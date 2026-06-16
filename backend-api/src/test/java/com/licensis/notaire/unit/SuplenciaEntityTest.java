package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Suplencia;
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
class SuplenciaEntityTest {

    private Date toDate(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            Suplencia suplencia = new Suplencia();

            assertThat(suplencia.getIdSuplencia()).isNull();
            assertThat(suplencia.getObservaciones()).isNull();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Suplencia suplencia = new Suplencia(55);

            assertThat(suplencia.getIdSuplencia()).isEqualTo(55);
        }

        @Test
        @DisplayName("Should initialize with ID, fechaInicio and fechaFin constructor")
        void shouldInitializeWithFullConstructor() {
            Date inicio = toDate(LocalDate.of(2026, 1, 1));
            Date fin = toDate(LocalDate.of(2026, 12, 31));

            Suplencia suplencia = new Suplencia(10, inicio, fin);

            assertThat(suplencia.getIdSuplencia()).isEqualTo(10);
            assertThat(suplencia.getFechaInicio()).isEqualTo(inicio);
            assertThat(suplencia.getFechaFin()).isEqualTo(fin);
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get dates")
        void shouldSetAndGetDates() {
            Date inicio = toDate(LocalDate.of(2026, 3, 1));
            Date fin = toDate(LocalDate.of(2026, 3, 31));

            Suplencia suplencia = new Suplencia();
            suplencia.setFechaInicio(inicio);
            suplencia.setFechaFin(fin);

            assertThat(suplencia.getFechaInicio()).isEqualTo(inicio);
            assertThat(suplencia.getFechaFin()).isEqualTo(fin);
        }

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            Suplencia suplencia = new Suplencia();
            suplencia.setObservaciones("Licencia por enfermedad");

            assertThat(suplencia.getObservaciones()).isEqualTo("Licencia por enfermedad");
        }

        @Test
        @DisplayName("Should set and get suplente")
        void shouldSetAndGetSuplente() {
            Persona suplente = new Persona();
            suplente.setNombre("Carlos");
            suplente.setApellido("Rodriguez");

            Suplencia suplencia = new Suplencia();
            suplencia.setFkIdSuplente(suplente);

            assertThat(suplencia.getFkIdSuplente()).isNotNull();
            assertThat(suplencia.getFkIdSuplente().getNombre()).isEqualTo("Carlos");
        }

        @Test
        @DisplayName("Should set and get suplantado")
        void shouldSetAndGetSuplantado() {
            Persona suplantado = new Persona();
            suplantado.setNombre("Maria");
            suplantado.setApellido("Gomez");

            Suplencia suplencia = new Suplencia();
            suplencia.setFkIdSuplantado(suplantado);

            assertThat(suplencia.getFkIdSuplantado()).isNotNull();
            assertThat(suplencia.getFkIdSuplantado().getNombre()).isEqualTo("Maria");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Suplencia suplencia = new Suplencia();
            suplencia.setVersion(2);

            assertThat(suplencia.getVersion()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Suplencia s1 = new Suplencia(10);
            Suplencia s2 = new Suplencia(10);

            assertThat(s1).isEqualTo(s2);
            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Suplencia s1 = new Suplencia(10);
            Suplencia s2 = new Suplencia(20);

            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Suplencia suplencia = new Suplencia(1);

            assertThat(suplencia).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Suplencia suplencia = new Suplencia(1);

            assertThat(suplencia).isNotEqualTo("string");
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Suplencia suplencia = new Suplencia(77);

            assertThat(suplencia.toString()).contains("77");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Suplencia suplencia = new Suplencia();

            assertThat(suplencia.hashCode()).isEqualTo(0);
        }
    }
}
