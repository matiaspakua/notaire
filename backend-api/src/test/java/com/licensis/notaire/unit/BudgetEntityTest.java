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
@DisplayName("Budget Entity Tests")
class BudgetEntityTest {

    @Nested
    @DisplayName("CU01 - Preparar Budget - Unit Tests")
    class PrepararBudgetTests {

        @Test
        @DisplayName("Should create budget with required fields")
        void shouldCreateBudgetWithRequiredFields() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setNumber(1001);
            budget.setDate(new Date());
            budget.setHeading("Compraventa de property");
            budget.setStatus("pendiente");
            budget.setPropertyAmount(new java.math.BigDecimal("500000.00"));

            assertThat(budget.getNumber()).isEqualTo(1001);
            assertThat(budget.getHeading()).isEqualTo("Compraventa de property");
            assertThat(budget.getStatus()).isEqualTo("pendiente");
            assertThat(budget.getPropertyAmount()).isEqualByComparingTo(new java.math.BigDecimal("500000.00"));
        }

        @Test
        @DisplayName("Should update budget status")
        void shouldUpdateBudgetStatus() {
            Budget budget = new Budget();
            budget.setIdBudget(1);
            budget.setNumber(1001);
            budget.setDate(new Date());
            budget.setHeading("Compraventa");
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
        @DisplayName("Should associate budget with more than one procedure")
        void shouldAssociateBudgetWithMultipleProcedures() {
            Budget budget = new Budget();
            budget.setIdBudget(1);

            Procedure procedure1 = new Procedure();
            procedure1.setIdProcedure(1);
            procedure1.setFkIdBudget(budget);

            Procedure procedure2 = new Procedure();
            procedure2.setIdProcedure(2);
            procedure2.setFkIdBudget(budget);

            budget.setProcedureList(List.of(procedure1, procedure2));

            assertThat(budget.getProcedureList()).containsExactly(procedure1, procedure2);
            assertThat(procedure1.getFkIdBudget()).isEqualTo(budget);
            assertThat(procedure2.getFkIdBudget()).isEqualTo(budget);
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
    @DisplayName("CU60 - Search Budget - Unit Tests")
    class SearchBudgetTests {

        @Test
        @DisplayName("Should filter budgets by person")
        void shouldFilterBudgetsByPerson() {
            Person person1 = new Person(1);
            Person person2 = new Person(2);

            Budget budget1 = new Budget(1);
            budget1.setFkIdPerson(person1);

            Budget budget2 = new Budget(2);
            budget2.setFkIdPerson(person2);

            Budget budget3 = new Budget(3);
            budget3.setFkIdPerson(person1);

            List<Budget> budgets = List.of(budget1, budget2, budget3);

            var filtered = budgets.stream()
                .filter(p -> p.getFkIdPerson() != null && p.getFkIdPerson().getPersonId().equals(1))
                .toList();

            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should filter budgets by status")
        void shouldFilterBudgetsByStatus() {
            Budget budget1 = new Budget(1);
            budget1.setStatus("pending");

            Budget budget2 = new Budget(2);
            budget2.setStatus("approved");

            List<Budget> budgets = List.of(budget1, budget2);

            var pending = budgets.stream()
                .filter(p -> "pending".equals(p.getStatus()))
                .toList();

            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).getIdBudget()).isEqualTo(1);
        }
    }
}
