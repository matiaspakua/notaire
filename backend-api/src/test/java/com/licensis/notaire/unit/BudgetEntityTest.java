package com.licensis.notaire.unit;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU01", "CU45", "CU60"})
@DisplayName("Presupuesto Entity Tests")
class BudgetEntityTest {

    @Nested
    @DisplayName("CU01 - Preparar Presupuesto - Unit Tests")
    class PrepararBudgetTests {

        @Test
        @DisplayName("Should create budget with required fields")
        void shouldCreateBudgetWithRequiredFields() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setNumber(1001);
            budget.setDate(new Date());
            budget.setEncabezado("Compraventa de property");
            budget.setStatus("pendiente");
            budget.setPropertyAmount(500000.00f);

            assertThat(budget.getNumber()).isEqualTo(1001);
            assertThat(budget.getEncabezado()).isEqualTo("Compraventa de property");
            assertThat(budget.getStatus()).isEqualTo("pendiente");
            assertThat(budget.getPropertyAmount()).isEqualTo(500000.00f);
        }

        @Test
        @DisplayName("Should update budget status")
        void shouldUpdateBudgetStatus() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setNumber(1001);
            budget.setDate(new Date());
            budget.setEncabezado("Compraventa");
            budget.setStatus("pendiente");

            budget.setStatus("aprobado");
            assertThat(budget.getStatus()).isEqualTo("aprobado");
        }

        @Test
        @DisplayName("Should link budget to person")
        void shouldLinkBudgetToPerson() {
            Person person = new Person();
            person.setPersonId(1);
            person.setFirstName("Juan");
            person.setLastName("Perez");

            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setFkIdPerson(person);

            assertThat(budget.getFkIdPerson()).isNotNull();
            assertThat(budget.getFkIdPerson().getFirstName()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Should associate budget with more than one tramite")
        void shouldAssociateBudgetWithMultipleProcedures() {
            Budget budget = new Budget();
            budget.setIdBudget(1);

            Procedure tramite1 = new Procedure();
            tramite1.setIdProcedure(1);
            tramite1.setFkIdBudget(budget);

            Procedure tramite2 = new Procedure();
            tramite2.setIdProcedure(2);
            tramite2.setFkIdBudget(budget);

            budget.setProcedureList(List.of(tramite1, tramite2));

            assertThat(budget.getProcedureList()).containsExactly(tramite1, tramite2);
            assertThat(tramite1.getFkIdBudget()).isEqualTo(budget);
            assertThat(tramite2.getFkIdBudget()).isEqualTo(budget);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Budget p1 = new Budget(1);
            Budget p2 = new Budget(1);
            Budget p3 = new Budget(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("Should initialize lists in constructor")
        void shouldInitializeListsInConstructor() {
            Budget budget = new Budget();

            assertThat(budget.getItemList()).isNotNull();
            assertThat(budget.getItemList()).isEmpty();
            assertThat(budget.getPaymentList()).isNotNull();
        }
    }

    @Nested
    @DisplayName("CU60 - Buscar Presupuesto - Unit Tests")
    class SearchBudgetTests {

        @Test
        @DisplayName("Should filter presupuestos by person")
        void shouldFilterPresupuestosByPerson() {
            Person persona1 = new Person(1);
            Person persona2 = new Person(2);

            Budget presupuesto1 = new Budget(1);
            presupuesto1.setFkIdPerson(persona1);

            Budget presupuesto2 = new Budget(2);
            presupuesto2.setFkIdPerson(persona2);

            Budget presupuesto3 = new Budget(3);
            presupuesto3.setFkIdPerson(persona1);

            List<Budget> presupuestos = List.of(presupuesto1, presupuesto2, presupuesto3);

            var filtered = presupuestos.stream()
                .filter(p -> p.getFkIdPerson() != null && p.getFkIdPerson().getPersonId().equals(1))
                .toList();

            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should filter presupuestos by status")
        void shouldFilterPresupuestosByStatus() {
            Budget presupuesto1 = new Budget(1);
            presupuesto1.setStatus("pendiente");

            Budget presupuesto2 = new Budget(2);
            presupuesto2.setStatus("aprobado");

            List<Budget> presupuestos = List.of(presupuesto1, presupuesto2);

            var pending = presupuestos.stream()
                .filter(p -> "pendiente".equals(p.getStatus()))
                .toList();

            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).getIdBudget()).isEqualTo(1);
        }
    }
}
