package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.negocio.Tramite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        @Test
        @DisplayName("Should have SIN_FIRMAR as default estado")
        void shouldHaveSinFirmarAsDefaultEstado() {
            Escritura escritura = new Escritura();

            assertThat(escritura.getEstado()).isEqualTo(ConstantesNegocio.ESCRITURA_SIN_FIRMAR);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Date fecha = new Date();
            Escritura escritura = new Escritura(7, 2025007, fecha, "Cuerpo notarial", "FIRMADA");

            assertThat(escritura.getIdEscritura()).isEqualTo(7);
            assertThat(escritura.getNumero()).isEqualTo(2025007);
            assertThat(escritura.getFechaEscrituracion()).isEqualTo(fecha);
            assertThat(escritura.getCuerpo()).isEqualTo("Cuerpo notarial");
            assertThat(escritura.getEstado()).isEqualTo("FIRMADA");
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get matriculaInscripcion")
        void shouldSetAndGetMatriculaInscripcion() {
            Escritura escritura = new Escritura();
            escritura.setMatriculaInscripcion("MAT-2025-001");

            assertThat(escritura.getMatriculaInscripcion()).isEqualTo("MAT-2025-001");
        }

        @Test
        @DisplayName("Should set and get fechaInscripcion")
        void shouldSetAndGetFechaInscripcion() {
            Date fecha = new Date();
            Escritura escritura = new Escritura();
            escritura.setFechaInscripcion(fecha);

            assertThat(escritura.getFechaInscripcion()).isEqualTo(fecha);
        }

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            Escritura escritura = new Escritura();
            escritura.setObservaciones("Pendiente de firma");

            assertThat(escritura.getObservaciones()).isEqualTo("Pendiente de firma");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Escritura escritura = new Escritura();
            escritura.setVersion(4);

            assertThat(escritura.getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should set and get folio list")
        void shouldSetAndGetFolioList() {
            List<Folio> folios = new ArrayList<>();
            folios.add(new Folio());

            Escritura escritura = new Escritura();
            escritura.setFolioList(folios);

            assertThat(escritura.getFolioList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get tramite list")
        void shouldSetAndGetTramiteList() {
            List<Tramite> tramites = new ArrayList<>();
            tramites.add(new Tramite(1));

            Escritura escritura = new Escritura();
            escritura.setTramiteList(tramites);

            assertThat(escritura.getTramiteList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get testimonio list")
        void shouldSetAndGetTestimonioList() {
            List<Testimonio> testimonios = new ArrayList<>();
            testimonios.add(new Testimonio());

            Escritura escritura = new Escritura();
            escritura.setTestimonioList(testimonios);

            assertThat(escritura.getTestimonioList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Escritura escritura = new Escritura(1);

            assertThat(escritura).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Escritura escritura = new Escritura(1);

            assertThat(escritura).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Escritura escritura = new Escritura();

            assertThat(escritura.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            Escritura e1 = new Escritura(3);
            Escritura e2 = new Escritura(3);

            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Escritura escritura = new Escritura(42);

            assertThat(escritura.toString()).contains("42");
        }
    }
}
