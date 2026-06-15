package com.licensis.notaire.service.unit;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.service.PersonaService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonaService Unit Tests")
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaService personaService;

    private Persona testPersona;
    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setIdTipoIdentificacion(1);
        tipoIdentificacion.setNombre("DNI");

        testPersona = new Persona();
        testPersona.setIdPersona(1);
        testPersona.setNombre("Juan");
        testPersona.setApellido("Pérez");
        testPersona.setNumeroIdentificacion("12345678");
        testPersona.setEsCliente(false);
        testPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
    }

    @Test
    @DisplayName("Should find all personas")
    void shouldFindAll() {
        List<Persona> personas = new ArrayList<>();
        personas.add(testPersona);

        when(personaRepository.findAll()).thenReturn(personas);

        List<Persona> result = personaService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no personas exist")
    void shouldReturnEmptyListWhenNoPersonasExist() {
        when(personaRepository.findAll()).thenReturn(new ArrayList<>());

        List<Persona> result = personaService.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(personaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find persona by id")
    void shouldFindPersonaById() {
        when(personaRepository.findById(1)).thenReturn(Optional.of(testPersona));

        Optional<Persona> result = personaService.findById(1);

        assertThat(result).isPresent()
                .contains(testPersona);

        verify(personaRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when persona not found")
    void shouldReturnEmptyOptionalWhenPersonaNotFound() {
        when(personaRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Persona> result = personaService.findById(999);

        assertThat(result).isEmpty();

        verify(personaRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should save persona")
    void shouldSavePersona() {
        when(personaRepository.save(testPersona)).thenReturn(testPersona);

        Persona result = personaService.save(testPersona);

        assertThat(result).isNotNull()
                .isEqualTo(testPersona)
                .extracting(Persona::getNombre, Persona::getApellido)
                .containsExactly("Juan", "Pérez");

        verify(personaRepository, times(1)).save(testPersona);
    }

    @Test
    @DisplayName("Should delete persona by id")
    void shouldDeletePersonaById() {
        personaService.deleteById(1);

        verify(personaRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should search by numero identificacion")
    void shouldSearchByNumeroIdentificacion() {
        when(personaRepository.findByNumeroIdentificacion("12345678"))
                .thenReturn(Optional.of(testPersona));

        List<Persona> result = personaService.buscar(null, null, "12345678", null, null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByNumeroIdentificacion("12345678");
    }

    @Test
    @DisplayName("Should return empty list when numero identificacion not found")
    void shouldReturnEmptyListWhenNumeroIdentificacionNotFound() {
        when(personaRepository.findByNumeroIdentificacion("99999999"))
                .thenReturn(Optional.empty());

        List<Persona> result = personaService.buscar(null, null, "99999999", null, null);

        assertThat(result).isNotNull()
                .isEmpty();

        verify(personaRepository, times(1)).findByNumeroIdentificacion("99999999");
    }

    @Test
    @DisplayName("Should search by nombre and apellido")
    void shouldSearchByNombreAndApellido() {
        List<Persona> expectedPersonas = new ArrayList<>();
        expectedPersonas.add(testPersona);

        when(personaRepository.findByNombreAndApellidoContainingIgnoreCase("Juan", "Pérez"))
                .thenReturn(expectedPersonas);

        List<Persona> result = personaService.buscar("Juan", "Pérez", null, null, null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByNombreAndApellidoContainingIgnoreCase("Juan", "Pérez");
        verify(personaRepository, never()).findByNumeroIdentificacion(anyString());
    }

    @Test
    @DisplayName("Should search by nombre only")
    void shouldSearchByNombreOnly() {
        List<Persona> expectedPersonas = new ArrayList<>();
        expectedPersonas.add(testPersona);

        when(personaRepository.findByNombreContainingIgnoreCase("Juan"))
                .thenReturn(expectedPersonas);

        List<Persona> result = personaService.buscar("Juan", null, null, null, null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByNombreContainingIgnoreCase("Juan");
    }

    @Test
    @DisplayName("Should search by apellido only")
    void shouldSearchByApellidoOnly() {
        List<Persona> expectedPersonas = new ArrayList<>();
        expectedPersonas.add(testPersona);

        when(personaRepository.findByApellidoContainingIgnoreCase("Pérez"))
                .thenReturn(expectedPersonas);

        List<Persona> result = personaService.buscar(null, "Pérez", null, null, null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByApellidoContainingIgnoreCase("Pérez");
    }

    @Test
    @DisplayName("Should search by tipo identificacion")
    void shouldSearchByTipoIdentificacion() {
        List<Persona> expectedPersonas = new ArrayList<>();
        expectedPersonas.add(testPersona);

        when(personaRepository.findByFkIdTipoIdentificacionIdTipoIdentificacion(1))
                .thenReturn(expectedPersonas);

        List<Persona> result = personaService.buscar(null, null, null, 1, null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1))
                .findByFkIdTipoIdentificacionIdTipoIdentificacion(1);
    }

    @Test
    @DisplayName("Should search by es cliente")
    void shouldSearchByEsCliente() {
        List<Persona> expectedPersonas = new ArrayList<>();
        expectedPersonas.add(testPersona);

        when(personaRepository.findByEsCliente(false))
                .thenReturn(expectedPersonas);

        List<Persona> result = personaService.buscar(null, null, null, null, false);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByEsCliente(false);
    }

    @Test
    @DisplayName("Should handle blank nombre and apellido")
    void shouldHandleBlankNombreAndApellido() {
        List<Persona> result = personaService.buscar("", "  ", null, null, null);

        assertThat(result).isNotNull()
                .isEmpty();

        verifyNoInteractions(personaRepository);
    }

    @Test
    @DisplayName("Should handle null filters")
    void shouldHandleNullFilters() {
        List<Persona> result = personaService.buscar(null, null, null, null, null);

        assertThat(result).isNotNull()
                .isEmpty();

        verifyNoInteractions(personaRepository);
    }

    @Test
    @DisplayName("Should prioritize numero identificacion in search")
    void shouldPrioritizeNumeroIdentificacionInSearch() {
        when(personaRepository.findByNumeroIdentificacion("12345678"))
                .thenReturn(Optional.of(testPersona));

        List<Persona> result = personaService.buscar("Juan", "Pérez", "12345678", 1, false);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testPersona);

        verify(personaRepository, times(1)).findByNumeroIdentificacion("12345678");
        verify(personaRepository, never()).findByNombreAndApellidoContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    @DisplayName("Should search by nombre and apellido even if other filters present")
    void shouldCombineMultipleSearchFilters() {
        List<Persona> personasNombreApellido = new ArrayList<>();
        personasNombreApellido.add(testPersona);

        when(personaRepository.findByNombreAndApellidoContainingIgnoreCase("Juan", "Pérez"))
                .thenReturn(personasNombreApellido);

        List<Persona> result = personaService.buscar("Juan", "Pérez", null, 1, null);

        assertThat(result).isNotNull()
                .contains(testPersona)
                .hasSize(1);

        verify(personaRepository, times(1))
                .findByNombreAndApellidoContainingIgnoreCase("Juan", "Pérez");
        verify(personaRepository, never())
                .findByFkIdTipoIdentificacionIdTipoIdentificacion(anyInt());
    }
}
