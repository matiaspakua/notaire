package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU01", "CU45", "CU60"})
@DisplayName("PresupuestoService unit tests")
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    private Budget testBudget;

    @BeforeEach
    void setUp() {
        testBudget = new Budget();
        testBudget.setIdBudget(1);
        testBudget.setNumber(1001);
        testBudget.setEncabezado("Presupuesto test");
        testBudget.setStatus("PENDIENTE");
        testBudget.setDate(new Date());
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all presupuestos")
        void shouldReturnAllPresupuestos() {
            when(budgetRepository.findAll()).thenReturn(List.of(testBudget));

            List<Budget> result = budgetService.findAll();

            assertThat(result).hasSize(1).containsExactly(testBudget);
            verify(budgetRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no presupuestos exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(budgetRepository.findAll()).thenReturn(List.of());

            assertThat(budgetService.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return budget when found")
        void shouldReturnBudgetWhenFound() {
            when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

            assertThat(budgetService.findById(1)).isPresent().contains(testBudget);
        }

        @Test
        @DisplayName("Should return empty when budget not found")
        void shouldReturnEmptyWhenNotFound() {
            when(budgetRepository.findById(999)).thenReturn(Optional.empty());

            assertThat(budgetService.findById(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPersona - CU60")
    class FindByPerson {

        @Test
        @DisplayName("Should return presupuestos for a given person")
        void shouldReturnPresupuestosForPerson() {
            when(budgetRepository.findByFkIdPersonIdPerson(5)).thenReturn(List.of(testBudget));

            List<Budget> result = budgetService.findByPerson(5);

            assertThat(result).hasSize(1).containsExactly(testBudget);
        }

        @Test
        @DisplayName("Should return empty list when person has no presupuestos")
        void shouldReturnEmptyWhenPersonHasNoPresupuestos() {
            when(budgetRepository.findByFkIdPersonIdPerson(99)).thenReturn(List.of());

            assertThat(budgetService.findByPerson(99)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEstado - CU60")
    class FindByStatus {

        @Test
        @DisplayName("Should return presupuestos filtered by status")
        void shouldFilterByStatus() {
            when(budgetRepository.findByStatus("PENDIENTE")).thenReturn(List.of(testBudget));

            List<Budget> result = budgetService.findByStatus("PENDIENTE");

            assertThat(result).hasSize(1).containsExactly(testBudget);
        }

        @Test
        @DisplayName("Should return all presupuestos when status is null")
        void shouldReturnAllWhenStatusIsNull() {
            when(budgetRepository.findAll()).thenReturn(List.of(testBudget));

            List<Budget> result = budgetService.findByStatus(null);

            assertThat(result).hasSize(1);
            verify(budgetRepository).findAll();
        }

        @Test
        @DisplayName("Should return all presupuestos when status is blank")
        void shouldReturnAllWhenStatusIsBlank() {
            when(budgetRepository.findAll()).thenReturn(List.of(testBudget));

            List<Budget> result = budgetService.findByStatus("   ");

            assertThat(result).hasSize(1);
            verify(budgetRepository).findAll();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should save budget and return it with generated ID")
        void shouldCreateBudget() {
            when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

            Budget result = budgetService.create(testBudget);

            assertThat(result).isNotNull();
            assertThat(result.getIdBudget()).isEqualTo(1);
            verify(budgetRepository).save(testBudget);
        }

        @Test
        @DisplayName("Should set default encabezado when blank")
        void shouldSetDefaultEncabezadoWhenBlank() {
            Budget p = new Budget();
            p.setNumber(100);
            p.setStatus("PENDIENTE");
            p.setDate(new Date());
            when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

            Budget result = budgetService.create(p);

            assertThat(result.getEncabezado()).isEqualTo("Presupuesto");
        }

        @Test
        @DisplayName("Should generate number when zero")
        void shouldGenerateNumberWhenZero() {
            Budget p = new Budget();
            p.setEncabezado("Test");
            p.setStatus("PENDIENTE");
            p.setDate(new Date());
            p.setNumber(0);
            when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

            Budget result = budgetService.create(p);

            assertThat(result.getNumber()).isNotZero();
        }

        @Test
        @DisplayName("Should keep provided encabezado when not blank")
        void shouldKeepProvidedEncabezado() {
            testBudget.setEncabezado("Presupuesto específico");
            when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

            Budget result = budgetService.create(testBudget);

            assertThat(result.getEncabezado()).isEqualTo("Presupuesto específico");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update and return budget when it exists")
        void shouldUpdateWhenExists() {
            when(budgetRepository.existsById(1)).thenReturn(true);
            when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

            Budget result = budgetService.update(1, testBudget);

            assertThat(result).isNotNull();
            assertThat(result.getIdBudget()).isEqualTo(1);
            verify(budgetRepository).save(testBudget);
        }

        @Test
        @DisplayName("Should set the ID from path variable before saving")
        void shouldSetIdBeforeSaving() {
            Budget incoming = new Budget();
            incoming.setStatus("APROBADO");
            when(budgetRepository.existsById(1)).thenReturn(true);
            when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

            Budget result = budgetService.update(1, incoming);

            assertThat(result.getIdBudget()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when budget does not exist")
        void shouldThrowWhenBudgetNotFound() {
            when(budgetRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> budgetService.update(999, testBudget))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should delete budget when it exists")
        void shouldDeleteWhenExists() {
            when(budgetRepository.existsById(1)).thenReturn(true);
            doNothing().when(budgetRepository).deleteById(1);

            budgetService.deleteById(1);

            verify(budgetRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when budget does not exist")
        void shouldThrowWhenBudgetNotFound() {
            when(budgetRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> budgetService.deleteById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }
}
