package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.PlantillaPresupuestoPK;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU06", "CU07"})
@DisplayName("PlantillaPresupuesto Entity Tests")
class PlantillaPresupuestoEntityTest {

    @Nested
    @DisplayName("PlantillaPresupuestoPK")
    class PrimaryKeyTests {

        @Test
        @DisplayName("Should create PK with default constructor")
        void shouldCreatePkWithDefaultConstructor() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK();

            assertThat(pk.getFkIdTipoTramite()).isEqualTo(0);
            assertThat(pk.getFkIdConcepto()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create PK with full constructor")
        void shouldCreatePkWithFullConstructor() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(5, 10);

            assertThat(pk.getFkIdTipoTramite()).isEqualTo(5);
            assertThat(pk.getFkIdConcepto()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should set and get PK fields")
        void shouldSetAndGetPkFields() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK();
            pk.setFkIdTipoTramite(3);
            pk.setFkIdConcepto(7);

            assertThat(pk.getFkIdTipoTramite()).isEqualTo(3);
            assertThat(pk.getFkIdConcepto()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should be equal when same fields")
        void shouldBeEqualWhenSameFields() {
            PlantillaPresupuestoPK pk1 = new PlantillaPresupuestoPK(2, 4);
            PlantillaPresupuestoPK pk2 = new PlantillaPresupuestoPK(2, 4);

            assertThat(pk1).isEqualTo(pk2);
            assertThat(pk1.hashCode()).isEqualTo(pk2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different fields")
        void shouldNotBeEqualWhenDifferentFields() {
            PlantillaPresupuestoPK pk1 = new PlantillaPresupuestoPK(1, 2);
            PlantillaPresupuestoPK pk2 = new PlantillaPresupuestoPK(1, 3);

            assertThat(pk1).isNotEqualTo(pk2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(1, 2);

            assertThat(pk).isNotEqualTo(null);
            assertThat(pk).isNotEqualTo("string");
        }

        @Test
        @DisplayName("toString should include field values")
        void toStringShouldIncludeFieldValues() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(5, 10);

            assertThat(pk.toString()).contains("5");
            assertThat(pk.toString()).contains("10");
        }
    }

    @Nested
    @DisplayName("PlantillaPresupuesto fields")
    class PlantillaFieldTests {

        @Test
        @DisplayName("Should create with default constructor")
        void shouldCreateWithDefaultConstructor() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto();

            assertThat(pp).isNotNull();
        }

        @Test
        @DisplayName("Should create with PK constructor")
        void shouldCreateWithPkConstructor() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(1, 2);
            PlantillaPresupuesto pp = new PlantillaPresupuesto(pk);

            assertThat(pp.getPlantillaPresupuestoPK()).isEqualTo(pk);
        }

        @Test
        @DisplayName("Should create with int PK constructor")
        void shouldCreateWithIntPkConstructor() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto(3, 7);

            assertThat(pp.getPlantillaPresupuestoPK()).isNotNull();
            assertThat(pp.getPlantillaPresupuestoPK().getFkIdTipoTramite()).isEqualTo(3);
            assertThat(pp.getPlantillaPresupuestoPK().getFkIdConcepto()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            pp.setObservaciones("Honorarios notariales estándar");

            assertThat(pp.getObservaciones()).isEqualTo("Honorarios notariales estándar");
        }

        @Test
        @DisplayName("Should set and get tipo de tramite")
        void shouldSetAndGetTipoDeTramite() {
            TipoDeTramite tipo = new TipoDeTramite();
            tipo.setNombre("Escritura de Venta");

            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            pp.setTipoDeTramite(tipo);

            assertThat(pp.getTipoDeTramite()).isNotNull();
            assertThat(pp.getTipoDeTramite().getNombre()).isEqualTo("Escritura de Venta");
        }

        @Test
        @DisplayName("Should set and get concepto")
        void shouldSetAndGetConcepto() {
            Concepto concepto = new Concepto();
            concepto.setNombre("Honorarios");

            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            pp.setConcepto(concepto);

            assertThat(pp.getConcepto()).isNotNull();
            assertThat(pp.getConcepto().getNombre()).isEqualTo("Honorarios");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            pp.setVersion(5);

            assertThat(pp.getVersion()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should be equal when same PK")
        void shouldBeEqualWhenSamePk() {
            PlantillaPresupuestoPK pk = new PlantillaPresupuestoPK(1, 2);
            PlantillaPresupuesto pp1 = new PlantillaPresupuesto(pk);
            PlantillaPresupuesto pp2 = new PlantillaPresupuesto(pk);

            assertThat(pp1).isEqualTo(pp2);
            assertThat(pp1.hashCode()).isEqualTo(pp2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different PK")
        void shouldNotBeEqualWhenDifferentPk() {
            PlantillaPresupuesto pp1 = new PlantillaPresupuesto(1, 2);
            PlantillaPresupuesto pp2 = new PlantillaPresupuesto(1, 3);

            assertThat(pp1).isNotEqualTo(pp2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto(1, 2);

            assertThat(pp).isNotEqualTo(null);
            assertThat(pp).isNotEqualTo("not a plantilla");
        }

        @Test
        @DisplayName("toString should be non-null")
        void toStringShouldBeNonNull() {
            PlantillaPresupuesto pp = new PlantillaPresupuesto(2, 4);

            assertThat(pp.toString()).isNotNull();
        }
    }
}
