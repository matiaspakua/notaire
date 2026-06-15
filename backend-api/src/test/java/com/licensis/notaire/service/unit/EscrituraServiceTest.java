package com.licensis.notaire.service.unit;

import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.service.EscrituraService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrituraService Unit Tests")
class EscrituraServiceTest {

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private EscrituraService escrituraService;

    private Escritura testEscritura;
    private Persona testEscribano;

    @BeforeEach
    void setUp() {
        testEscribano = new Persona();
        testEscribano.setIdPersona(1);
        testEscribano.setNombre("Juan");
        testEscribano.setApellido("Escribano");
        testEscribano.setRegistroEscribano(100);

        testEscritura = new Escritura();
        testEscritura.setIdEscritura(1);
        testEscritura.setNumero(100);
        testEscritura.setCuerpo("Venta de propiedad");
        testEscritura.setEstado("FIRMADA");
    }

    @Test
    @DisplayName("Should find all escrituras with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Escritura> page = new PageImpl<>(List.of(testEscritura), pageable, 1);

        when(escrituraRepository.findAll(pageable)).thenReturn(page);

        Page<Escritura> result = escrituraService.findAllPaged(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testEscritura);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(escrituraRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all escrituras")
    void shouldFindAll() {
        List<Escritura> escrituras = new ArrayList<>();
        escrituras.add(testEscritura);

        when(escrituraRepository.findAll()).thenReturn(escrituras);

        List<Escritura> result = escrituraService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testEscritura);

        verify(escrituraRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no escrituras exist")
    void shouldReturnEmptyListWhenNoEscriturasExist() {
        when(escrituraRepository.findAll()).thenReturn(new ArrayList<>());

        List<Escritura> result = escrituraService.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(escrituraRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find escritura by id")
    void shouldFindEscrituraById() {
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(testEscritura));

        Optional<Escritura> result = escrituraService.findById(1);

        assertThat(result).isPresent()
                .contains(testEscritura);

        verify(escrituraRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when escritura not found")
    void shouldReturnEmptyOptionalWhenEscrituraNotFound() {
        when(escrituraRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Escritura> result = escrituraService.findById(999);

        assertThat(result).isEmpty();

        verify(escrituraRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should save escritura")
    void shouldSaveEscritura() {
        when(escrituraRepository.save(testEscritura)).thenReturn(testEscritura);

        Escritura result = escrituraService.save(testEscritura);

        assertThat(result).isNotNull()
                .isEqualTo(testEscritura)
                .extracting(Escritura::getNumero)
                .isEqualTo(100);

        verify(escrituraRepository, times(1)).save(testEscritura);
    }

    @Test
    @DisplayName("Should delete escritura by id")
    void shouldDeleteEscrituraById() {
        escrituraService.deleteById(1);

        verify(escrituraRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should find all available escribanos")
    void shouldFindEscribanosDisponibles() {
        List<Persona> escribanos = new ArrayList<>();
        escribanos.add(testEscribano);

        when(personaRepository.findAllEscribanos()).thenReturn(escribanos);

        List<Persona> result = escrituraService.findEscribanosDisponibles();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testEscribano)
                .allMatch(p -> p.getRegistroEscribano() != null);

        verify(personaRepository, times(1)).findAllEscribanos();
    }

    @Test
    @DisplayName("Should return empty list when no escribanos available")
    void shouldReturnEmptyListWhenNoEscribanosAvailable() {
        when(personaRepository.findAllEscribanos()).thenReturn(new ArrayList<>());

        List<Persona> result = escrituraService.findEscribanosDisponibles();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(personaRepository, times(1)).findAllEscribanos();
    }

    @Test
    @DisplayName("Should search escritura by numero")
    void shouldSearchEscrituraByNumero() {
        when(escrituraRepository.findByNumero(100))
                .thenReturn(Optional.of(testEscritura));

        List<Escritura> result = escrituraService.buscarPorNumero(100);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testEscritura);

        verify(escrituraRepository, times(1)).findByNumero(100);
        verify(escrituraRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return empty list when escritura numero not found")
    void shouldReturnEmptyListWhenEscrituraNumernoNotFound() {
        when(escrituraRepository.findByNumero(999))
                .thenReturn(Optional.empty());

        List<Escritura> result = escrituraService.buscarPorNumero(999);

        assertThat(result).isNotNull()
                .isEmpty();

        verify(escrituraRepository, times(1)).findByNumero(999);
    }

    @Test
    @DisplayName("Should return all escrituras when numero is null")
    void shouldReturnAllEscriturasWhenNumeroIsNull() {
        List<Escritura> escrituras = new ArrayList<>();
        escrituras.add(testEscritura);

        when(escrituraRepository.findAll()).thenReturn(escrituras);

        List<Escritura> result = escrituraService.buscarPorNumero(null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testEscritura);

        verify(escrituraRepository, times(1)).findAll();
        verify(escrituraRepository, never()).findByNumero(anyInt());
    }
}
