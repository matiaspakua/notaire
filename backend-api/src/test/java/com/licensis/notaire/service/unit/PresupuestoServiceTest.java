package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.PresupuestoService;
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
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @InjectMocks
    private PresupuestoService presupuestoService;

    private Presupuesto testPresupuesto;

    @BeforeEach
    void setUp() {
        testPresupuesto = new Presupuesto();
        testPresupuesto.setIdPresupuesto(1);
        testPresupuesto.setNumero(1000);
        testPresupuesto.setEncabezado("Presupuesto Venta");
        testPresupuesto.setEstado("APROBADO");
    }

    @Test
    @DisplayName("Should find all presupuestos")
    void shouldFindAll() {
        List<Presupuesto> presupuestos = new ArrayList<>();
        presupuestos.add(testPresupuesto);

        when(presupuestoRepository.findAll()).thenReturn(presupuestos);

        List<Presupuesto> result = presupuestoService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto);

        verify(presupuestoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no presupuestos exist")
    void shouldReturnEmptyListWhenNoPresupuestosExist() {
        when(presupuestoRepository.findAll()).thenReturn(new ArrayList<>());

        List<Presupuesto> result = presupuestoService.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(presupuestoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find presupuestos with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Presupuesto> page = new PageImpl<>(List.of(testPresupuesto), pageable, 1);

        when(presupuestoRepository.findAll(pageable)).thenReturn(page);

        Page<Presupuesto> result = presupuestoService.findAllPaged(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);

        verify(presupuestoRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find presupuesto by id")
    void shouldFindPresupuestoById() {
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

        Optional<Presupuesto> result = presupuestoService.findById(1);

        assertThat(result).isPresent()
                .contains(testPresupuesto);

        verify(presupuestoRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when presupuesto not found")
    void shouldReturnEmptyOptionalWhenPresupuestoNotFound() {
        when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Presupuesto> result = presupuestoService.findById(999);

        assertThat(result).isEmpty();

        verify(presupuestoRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find presupuestos by persona")
    void shouldFindPresupuestosByPersona() {
        List<Presupuesto> presupuestos = new ArrayList<>();
        presupuestos.add(testPresupuesto);

        when(presupuestoRepository.findByFkIdPersonaIdPersona(1))
                .thenReturn(presupuestos);

        List<Presupuesto> result = presupuestoService.findByPersona(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto);

        verify(presupuestoRepository, times(1)).findByFkIdPersonaIdPersona(1);
    }

    @Test
    @DisplayName("Should return empty list when persona has no presupuestos")
    void shouldReturnEmptyListWhenPersonaHasNoPresupuestos() {
        when(presupuestoRepository.findByFkIdPersonaIdPersona(999))
                .thenReturn(new ArrayList<>());

        List<Presupuesto> result = presupuestoService.findByPersona(999);

        assertThat(result).isNotNull()
                .isEmpty();

        verify(presupuestoRepository, times(1)).findByFkIdPersonaIdPersona(999);
    }

    @Test
    @DisplayName("Should find presupuestos by estado")
    void shouldFindPresupuestosByEstado() {
        List<Presupuesto> presupuestos = new ArrayList<>();
        presupuestos.add(testPresupuesto);

        when(presupuestoRepository.findByEstado("APROBADO"))
                .thenReturn(presupuestos);

        List<Presupuesto> result = presupuestoService.findByEstado("APROBADO");

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto)
                .allMatch(p -> p.getEstado().equals("APROBADO"));

        verify(presupuestoRepository, times(1)).findByEstado("APROBADO");
    }

    @Test
    @DisplayName("Should return all presupuestos when estado is null")
    void shouldReturnAllPresupuestosWhenEstadoIsNull() {
        List<Presupuesto> presupuestos = new ArrayList<>();
        presupuestos.add(testPresupuesto);

        when(presupuestoRepository.findAll()).thenReturn(presupuestos);

        List<Presupuesto> result = presupuestoService.findByEstado(null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto);

        verify(presupuestoRepository, times(1)).findAll();
        verify(presupuestoRepository, never()).findByEstado(anyString());
    }

    @Test
    @DisplayName("Should return all presupuestos when estado is blank")
    void shouldReturnAllPresupuestosWhenEstadoIsBlank() {
        List<Presupuesto> presupuestos = new ArrayList<>();
        presupuestos.add(testPresupuesto);

        when(presupuestoRepository.findAll()).thenReturn(presupuestos);

        List<Presupuesto> result = presupuestoService.findByEstado("  ");

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPresupuesto);

        verify(presupuestoRepository, times(1)).findAll();
        verify(presupuestoRepository, never()).findByEstado(anyString());
    }

    @Test
    @DisplayName("Should create presupuesto with default encabezado")
    void shouldCreatePresupuestoWithDefaultEncabezado() {
        Presupuesto newPresupuesto = new Presupuesto();
        newPresupuesto.setEncabezado(null);
        newPresupuesto.setNumero(0);

        when(presupuestoRepository.save(any(Presupuesto.class)))
                .thenAnswer(inv -> {
                    Presupuesto p = inv.getArgument(0);
                    p.setIdPresupuesto(1);
                    return p;
                });

        Presupuesto result = presupuestoService.create(newPresupuesto);

        assertThat(result).isNotNull()
                .extracting(Presupuesto::getEncabezado)
                .isEqualTo("Presupuesto");
        assertThat(result.getNumero()).isNotEqualTo(0);

        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    @DisplayName("Should create presupuesto with provided encabezado")
    void shouldCreatePresupuestoWithProvidedEncabezado() {
        Presupuesto newPresupuesto = new Presupuesto();
        newPresupuesto.setEncabezado("Custom");
        newPresupuesto.setNumero(0);

        when(presupuestoRepository.save(any(Presupuesto.class)))
                .thenAnswer(inv -> {
                    Presupuesto p = inv.getArgument(0);
                    p.setIdPresupuesto(1);
                    return p;
                });

        Presupuesto result = presupuestoService.create(newPresupuesto);

        assertThat(result).isNotNull()
                .extracting(Presupuesto::getEncabezado)
                .isEqualTo("Custom");

        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    @DisplayName("Should generate numero when creating presupuesto")
    void shouldGenerateNumeroWhenCreatingPresupuesto() {
        Presupuesto newPresupuesto = new Presupuesto();
        newPresupuesto.setEncabezado("Test");
        newPresupuesto.setNumero(0);

        when(presupuestoRepository.save(any(Presupuesto.class)))
                .thenAnswer(inv -> {
                    Presupuesto p = inv.getArgument(0);
                    p.setIdPresupuesto(1);
                    return p;
                });

        Presupuesto result = presupuestoService.create(newPresupuesto);

        assertThat(result.getNumero()).isGreaterThan(0)
                .isLessThan(Integer.MAX_VALUE);

        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    @DisplayName("Should update presupuesto")
    void shouldUpdatePresupuesto() {
        Presupuesto updatedPresupuesto = new Presupuesto();
        updatedPresupuesto.setNumero(2000);
        updatedPresupuesto.setEstado("RECHAZADO");

        when(presupuestoRepository.existsById(1)).thenReturn(true);
        when(presupuestoRepository.save(any(Presupuesto.class)))
                .thenReturn(updatedPresupuesto);

        Presupuesto result = presupuestoService.update(1, updatedPresupuesto);

        assertThat(result).isNotNull()
                .extracting(Presupuesto::getIdPresupuesto)
                .isEqualTo(1);
        assertThat(result.getEstado()).isEqualTo("RECHAZADO");

        verify(presupuestoRepository, times(1)).existsById(1);
        verify(presupuestoRepository, times(1)).save(any(Presupuesto.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent presupuesto")
    void shouldThrowExceptionWhenUpdatingNonExistentPresupuesto() {
        when(presupuestoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> presupuestoService.update(999, testPresupuesto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(presupuestoRepository, times(1)).existsById(999);
        verify(presupuestoRepository, never()).save(any(Presupuesto.class));
    }

    @Test
    @DisplayName("Should delete presupuesto")
    void shouldDeletePresupuesto() {
        when(presupuestoRepository.existsById(1)).thenReturn(true);

        presupuestoService.deleteById(1);

        verify(presupuestoRepository, times(1)).existsById(1);
        verify(presupuestoRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent presupuesto")
    void shouldThrowExceptionWhenDeletingNonExistentPresupuesto() {
        when(presupuestoRepository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> presupuestoService.deleteById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Presupuesto no encontrado");

        verify(presupuestoRepository, times(1)).existsById(999);
        verify(presupuestoRepository, never()).deleteById(anyInt());
    }
}
