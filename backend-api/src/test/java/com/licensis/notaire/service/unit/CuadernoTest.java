package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.CuadernoRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.service.CuadernoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuadernoService Unit Tests")
class CuadernoTest {

    @Mock
    private CuadernoRepository cuadernoRepository;

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private CuadernoService cuadernoService;

    private Persona escribano;

    @BeforeEach
    void setUp() {
        escribano = new Persona();
        escribano.setIdPersona(1);
        escribano.setRegistroEscribano(7);
    }

    private Folio folio(Integer id, int numero, String estado) {
        Folio folio = new Folio();
        folio.setIdFolio(id);
        folio.setNumero(numero);
        folio.setAnio(2026);
        folio.setEstado(estado);
        folio.setFkIdPersonaEscribano(escribano);
        return folio;
    }

    @Test
    @DisplayName("Should mark all folios as Asignado a cuaderno")
    void shouldMarkFoliosAsAsignadoACuaderno() {
        List<Folio> folios = new ArrayList<>(List.of(folio(1, 1, "Nuevo"), folio(2, 2, "Nuevo")));
        Cuaderno cuaderno = new Cuaderno();
        cuaderno.setIdCuaderno(10);

        cuadernoService.marcarFoliosAsignados(folios, cuaderno);

        assertThat(folios).allSatisfy(f -> {
            assertThat(f.getEstado()).isEqualTo("Asignado a cuaderno");
            assertThat(f.getFkIdCuaderno()).isEqualTo(cuaderno);
        });
    }

    @Test
    @DisplayName("Should assign number one to the first cuaderno of the year for a registro")
    void shouldAssignNumberOneToFirstCuadernoOfYear() {
        when(cuadernoRepository.findByAnioAndFkIdPersonaEscribano(2026, escribano)).thenReturn(List.of());
        when(cuadernoRepository.existsByNumeroAndAnioAndFkIdPersonaEscribano(1, 2026, escribano)).thenReturn(false);

        int numero = cuadernoService.calcularSiguienteNumero(2026, escribano);

        assertThat(numero).isEqualTo(1);
    }

    @Test
    @DisplayName("Should recalculate the next available cuaderno number on conflict")
    void shouldRecalculateNextAvailableCuadernoNumber() {
        when(cuadernoRepository.findByAnioAndFkIdPersonaEscribano(2026, escribano))
                .thenReturn(List.of(new Cuaderno()));
        when(cuadernoRepository.existsByNumeroAndAnioAndFkIdPersonaEscribano(2, 2026, escribano)).thenReturn(true);
        when(cuadernoRepository.existsByNumeroAndAnioAndFkIdPersonaEscribano(3, 2026, escribano)).thenReturn(false);

        int numero = cuadernoService.calcularSiguienteNumero(2026, escribano);

        assertThat(numero).isEqualTo(3);
    }

    @Test
    @DisplayName("Should reject cuaderno creation when escribano does not exist")
    void shouldRejectCreationWhenEscribanoNotFound() {
        when(personaRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuadernoService.crearCuaderno(List.of(1, 2), 99, 2026, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject cuaderno creation when folio count is not a multiple of ten")
    void shouldRejectWhenFolioCountNotMultipleOfTen() {
        when(personaRepository.findById(1)).thenReturn(Optional.of(escribano));
        List<Folio> folios = List.of(folio(1, 1, "Nuevo"), folio(2, 2, "Nuevo"));
        when(folioRepository.findAllByIdFolioIn(List.of(1, 2))).thenReturn(folios);

        assertThatThrownBy(() -> cuadernoService.crearCuaderno(List.of(1, 2), 1, 2026, null))
                .isInstanceOf(BusinessValidationException.class);
    }
}
