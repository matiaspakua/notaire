package com.licensis.notaire.unit;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import com.licensis.notaire.business.BudgetTemplatePK;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU06", "CU07"})
@DisplayName("PlantillaPresupuesto Entity Tests")
class BudgetTemplateEntityTest {

    @Nested
    @DisplayName("PlantillaPresupuestoPK")
    class PrimaryKeyTests {

        @Test
        @DisplayName("Should create PK with default constructor")
        void shouldCreatePkWithDefaultConstructor() {
            BudgetTemplatePK pk = new BudgetTemplatePK();

            assertThat(pk.getFkIdProcedureType()).isEqualTo(0);
            assertThat(pk.getFkIdConcept()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create PK with full constructor")
        void shouldCreatePkWithFullConstructor() {
            BudgetTemplatePK pk = new BudgetTemplatePK(5, 10);

            assertThat(pk.getFkIdProcedureType()).isEqualTo(5);
            assertThat(pk.getFkIdConcept()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should set and get PK fields")
        void shouldSetAndGetPkFields() {
            BudgetTemplatePK pk = new BudgetTemplatePK();
            pk.setFkIdProcedureType(3);
            pk.setFkIdConcept(7);

            assertThat(pk.getFkIdProcedureType()).isEqualTo(3);
            assertThat(pk.getFkIdConcept()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should be equal when same fields")
        void shouldBeEqualWhenSameFields() {
            BudgetTemplatePK pk1 = new BudgetTemplatePK(2, 4);
            BudgetTemplatePK pk2 = new BudgetTemplatePK(2, 4);

            assertThat(pk1).isEqualTo(pk2);
            assertThat(pk1.hashCode()).isEqualTo(pk2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different fields")
        void shouldNotBeEqualWhenDifferentFields() {
            BudgetTemplatePK pk1 = new BudgetTemplatePK(1, 2);
            BudgetTemplatePK pk2 = new BudgetTemplatePK(1, 3);

            assertThat(pk1).isNotEqualTo(pk2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            BudgetTemplatePK pk = new BudgetTemplatePK(1, 2);

            assertThat(pk).isNotEqualTo(null);
            assertThat(pk).isNotEqualTo("string");
        }

        @Test
        @DisplayName("toString should include field values")
        void toStringShouldIncludeFieldValues() {
            BudgetTemplatePK pk = new BudgetTemplatePK(5, 10);

            assertThat(pk.toString()).contains("5");
            assertThat(pk.toString()).contains("10");
        }
    }

    @Nested
    @DisplayName("PlantillaPresupuesto fields")
    class TemplateFieldTests {

        @Test
        @DisplayName("Should create with default constructor")
        void shouldCreateWithDefaultConstructor() {
            BudgetTemplate pp = new BudgetTemplate();

            assertThat(pp).isNotNull();
        }

        @Test
        @DisplayName("Should create with PK constructor")
        void shouldCreateWithPkConstructor() {
            BudgetTemplatePK pk = new BudgetTemplatePK(1, 2);
            BudgetTemplate pp = new BudgetTemplate(pk);

            assertThat(pp.getBudgetTemplatePK()).isEqualTo(pk);
        }

        @Test
        @DisplayName("Should create with int PK constructor")
        void shouldCreateWithIntPkConstructor() {
            BudgetTemplate pp = new BudgetTemplate(3, 7);

            assertThat(pp.getBudgetTemplatePK()).isNotNull();
            assertThat(pp.getBudgetTemplatePK().getFkIdProcedureType()).isEqualTo(3);
            assertThat(pp.getBudgetTemplatePK().getFkIdConcept()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            BudgetTemplate pp = new BudgetTemplate();
            pp.setNotes("Honorarios notariales estándar");

            assertThat(pp.getNotes()).isEqualTo("Honorarios notariales estándar");
        }

        @Test
        @DisplayName("Should set and get type de tramite")
        void shouldSetAndGetProcedureType() {
            ProcedureType type = new ProcedureType();
            type.setName("Escritura de Venta");

            BudgetTemplate pp = new BudgetTemplate();
            pp.setProcedureType(type);

            assertThat(pp.getProcedureType()).isNotNull();
            assertThat(pp.getProcedureType().getName()).isEqualTo("Escritura de Venta");
        }

        @Test
        @DisplayName("Should set and get concepto")
        void shouldSetAndGetConcept() {
            Concept concept = new Concept();
            concept.setName("Honorarios");

            BudgetTemplate pp = new BudgetTemplate();
            pp.setConcept(concept);

            assertThat(pp.getConcept()).isNotNull();
            assertThat(pp.getConcept().getName()).isEqualTo("Honorarios");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            BudgetTemplate pp = new BudgetTemplate();
            pp.setVersion(5);

            assertThat(pp.getVersion()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should be equal when same PK")
        void shouldBeEqualWhenSamePk() {
            BudgetTemplatePK pk = new BudgetTemplatePK(1, 2);
            BudgetTemplate pp1 = new BudgetTemplate(pk);
            BudgetTemplate pp2 = new BudgetTemplate(pk);

            assertThat(pp1).isEqualTo(pp2);
            assertThat(pp1.hashCode()).isEqualTo(pp2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different PK")
        void shouldNotBeEqualWhenDifferentPk() {
            BudgetTemplate pp1 = new BudgetTemplate(1, 2);
            BudgetTemplate pp2 = new BudgetTemplate(1, 3);

            assertThat(pp1).isNotEqualTo(pp2);
        }

        @Test
        @DisplayName("Should not be equal to null or different type")
        void shouldNotBeEqualToNullOrDifferentType() {
            BudgetTemplate pp = new BudgetTemplate(1, 2);

            assertThat(pp).isNotEqualTo(null);
            assertThat(pp).isNotEqualTo("not a plantilla");
        }

        @Test
        @DisplayName("toString should be non-null")
        void toStringShouldBeNonNull() {
            BudgetTemplate pp = new BudgetTemplate(2, 4);

            assertThat(pp.toString()).isNotNull();
        }
    }
}
