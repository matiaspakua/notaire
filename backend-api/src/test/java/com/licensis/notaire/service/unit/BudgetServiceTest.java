package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PresupuestoService Unit Tests")
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
        testBudget.setNumber(1000);
        testBudget.setEncabezado("Presupuesto Venta");
        testBudget.setStatus("APROBADO");
    }

    @Test
    @DisplayName("Should find all presupuestos")
    void shouldFindAll() {
        List<Budget> presupuestos = new ArrayList<>();
        presupuestos.add(testBudget);

        when(budgetRepository.findAll()).thenReturn(presupuestos);

        List<Budget> result = budgetService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget);

        verify(budgetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no presupuestos exist")
    void shouldReturnEmptyListWhenNoPresupuestosExist() {
        when(budgetRepository.findAll()).thenReturn(new ArrayList<>());

        List<Budget> result = budgetService.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(budgetRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find presupuestos with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Budget> page = new PageImpl<>(List.of(testBudget), pageable, 1);

        when(budgetRepository.findAll(pageable)).thenReturn(page);

        Page<Budget> result = budgetService.findAllPaged(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);

        verify(budgetRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find budget by id")
    void shouldFindBudgetById() {
        when(budgetRepository.findById(1)).thenReturn(Optional.of(testBudget));

        Optional<Budget> result = budgetService.findById(1);

        assertThat(result).isPresent()
                .contains(testBudget);

        verify(budgetRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when budget not found")
    void shouldReturnEmptyOptionalWhenBudgetNotFound() {
        when(budgetRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Budget> result = budgetService.findById(999);

        assertThat(result).isEmpty();

        verify(budgetRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find presupuestos by person")
    void shouldFindPresupuestosByPerson() {
        List<Budget> presupuestos = new ArrayList<>();
        presupuestos.add(testBudget);

        when(budgetRepository.findByFkIdPersonIdPerson(1))
                .thenReturn(presupuestos);

        List<Budget> result = budgetService.findByPerson(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget);

        verify(budgetRepository, times(1)).findByFkIdPersonIdPerson(1);
    }

    @Test
    @DisplayName("Should return empty list when person has no presupuestos")
    void shouldReturnEmptyListWhenPersonHasNoPresupuestos() {
        when(budgetRepository.findByFkIdPersonIdPerson(999))
                .thenReturn(new ArrayList<>());

        List<Budget> result = budgetService.findByPerson(999);

        assertThat(result).isNotNull()
                .isEmpty();

        verify(budgetRepository, times(1)).findByFkIdPersonIdPerson(999);
    }

    @Test
    @DisplayName("Should find presupuestos by status")
    void shouldFindPresupuestosByStatus() {
        List<Budget> presupuestos = new ArrayList<>();
        presupuestos.add(testBudget);

        when(budgetRepository.findByStatus("APROBADO"))
                .thenReturn(presupuestos);

        List<Budget> result = budgetService.findByStatus("APROBADO");

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget)
                .allMatch(p -> p.getStatus().equals("APROBADO"));

        verify(budgetRepository, times(1)).findByStatus("APROBADO");
    }

    @Test
    @DisplayName("Should return all presupuestos when status is null")
    void shouldReturnAllPresupuestosWhenStatusIsNull() {
        List<Budget> presupuestos = new ArrayList<>();
        presupuestos.add(testBudget);

        when(budgetRepository.findAll()).thenReturn(presupuestos);

        List<Budget> result = budgetService.findByStatus(null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget);

        verify(budgetRepository, times(1)).findAll();
        verify(budgetRepository, never()).findByStatus(anyString());
    }

    @Test
    @DisplayName("Should return all presupuestos when status is blank")
    void shouldReturnAllPresupuestosWhenStatusIsBlank() {
        List<Budget> presupuestos = new ArrayList<>();
        presupuestos.add(testBudget);

        when(budgetRepository.findAll()).thenReturn(presupuestos);

        List<Budget> result = budgetService.findByStatus("  ");

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testBudget);

        verify(budgetRepository, times(1)).findAll();
        verify(budgetRepository, never()).findByStatus(anyString());
    }

    @Test
    @DisplayName("Should create budget with default encabezado")
    void shouldCreateBudgetWithDefaultEncabezado() {
        Budget newBudget = new Budget();
        newBudget.setEncabezado(null);
        newBudget.setNumber(0);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(inv -> {
                    Budget p = inv.getArgument(0);
                    p.setIdBudget(1);
                    return p;
                });

        Budget result = budgetService.create(newBudget);

        assertThat(result).isNotNull()
                .extracting(Budget::getEncabezado)
                .isEqualTo("Presupuesto");
        assertThat(result.getNumber()).isNotEqualTo(0);

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should create budget with provided encabezado")
    void shouldCreateBudgetWithProvidedEncabezado() {
        Budget newBudget = new Budget();
        newBudget.setEncabezado("Custom");
        newBudget.setNumber(0);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(inv -> {
                    Budget p = inv.getArgument(0);
                    p.setIdBudget(1);
                    return p;
                });

        Budget result = budgetService.create(newBudget);

        assertThat(result).isNotNull()
                .extracting(Budget::getEncabezado)
                .isEqualTo("Custom");

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should generate number when creating budget")
    void shouldGenerateNumberWhenCreatingBudget() {
        Budget newBudget = new Budget();
        newBudget.setEncabezado("Test");
        newBudget.setNumber(0);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(inv -> {
                    Budget p = inv.getArgument(0);
                    p.setIdBudget(1);
                    return p;
                });

        Budget result = budgetService.create(newBudget);

        assertThat(result.getNumber()).isGreaterThan(0)
                .isLessThan(Integer.MAX_VALUE);

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should update budget")
    void shouldUpdateBudget() {
        Budget updatedBudget = new Budget();
        updatedBudget.setNumber(2000);
        updatedBudget.setStatus("RECHAZADO");

        when(budgetRepository.existsById(1)).thenReturn(true);
        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(updatedBudget);

        Budget result = budgetService.update(1, updatedBudget);

        assertThat(result).isNotNull()
                .extracting(Budget::getIdBudget)
                .isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo("RECHAZADO");

        verify(budgetRepository, times(1)).existsById(1);
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent budget")
    void shouldThrowExceptionWhenUpdatingNonExistentBudget() {
        when(budgetRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> budgetService.update(999, testBudget))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(budgetRepository, times(1)).existsById(999);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @DisplayName("Should delete budget")
    void shouldDeleteBudget() {
        when(budgetRepository.existsById(1)).thenReturn(true);

        budgetService.deleteById(1);

        verify(budgetRepository, times(1)).existsById(1);
        verify(budgetRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent budget")
    void shouldThrowExceptionWhenDeletingNonExistentBudget() {
        when(budgetRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> budgetService.deleteById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(budgetRepository, times(1)).existsById(999);
        verify(budgetRepository, never()).deleteById(anyInt());
    }
}
