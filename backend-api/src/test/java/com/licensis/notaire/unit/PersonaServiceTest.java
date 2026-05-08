package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.service.PersonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PersonaService unit tests")
@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaService personaService;

    private Persona testPersona;

    @BeforeEach
    void setUp() {
        testPersona = new Persona();
        testPersona.setIdPersona(1);
        testPersona.setNombre("Ana");
        testPersona.setApellido("Lopez");
        testPersona.setNumeroIdentificacion("12345678");
        testPersona.setEsCliente(true);
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all personas")
        void shouldReturnAllPersonas() {
            when(personaRepository.findAll()).thenReturn(List.of(testPersona));

            List<Persona> result = personaService.findAll();

            assertThat(result).hasSize(1).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should return empty list when no personas exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(personaRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(personaService.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return persona when found")
        void shouldReturnPersonaWhenFound() {
            when(personaRepository.findById(1)).thenReturn(Optional.of(testPersona));

            assertThat(personaService.findById(1)).isPresent().contains(testPersona);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(personaRepository.findById(999)).thenReturn(Optional.empty());

            assertThat(personaService.findById(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return the persona")
        void shouldSaveAndReturnPersona() {
            when(personaRepository.save(any(Persona.class))).thenReturn(testPersona);

            Persona result = personaService.save(testPersona);

            assertThat(result).isEqualTo(testPersona);
            verify(personaRepository).save(testPersona);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should delegate to repository deleteById")
        void shouldDeletePersona() {
            doNothing().when(personaRepository).deleteById(1);

            personaService.deleteById(1);

            verify(personaRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("buscar - CU18/CU41/CU54/CU61")
    class Buscar {

        @Test
        @DisplayName("Should search by numeroIdentificacion when provided")
        void shouldSearchByNumeroIdentificacionWhenProvided() {
            when(personaRepository.findByNumeroIdentificacion("12345678"))
                    .thenReturn(Optional.of(testPersona));

            List<Persona> result = personaService.buscar(null, null, "12345678", null, null);

            assertThat(result).hasSize(1).containsExactly(testPersona);
            verify(personaRepository).findByNumeroIdentificacion("12345678");
            verify(personaRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should return empty list when numeroIdentificacion not found")
        void shouldReturnEmptyWhenNumeroIdentificacionNotFound() {
            when(personaRepository.findByNumeroIdentificacion("99999999")).thenReturn(Optional.empty());

            List<Persona> result = personaService.buscar(null, null, "99999999", null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should search by nombre and apellido when both provided")
        void shouldSearchByNombreAndApellidoWhenBothProvided() {
            when(personaRepository.findByNombreAndApellidoContainingIgnoreCase("Ana", "Lopez"))
                    .thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar("Ana", "Lopez", null, null, null);

            assertThat(result).hasSize(1).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should search by nombre only when apellido not provided")
        void shouldSearchByNombreOnlyWhenApellidoNotProvided() {
            when(personaRepository.findByNombreContainingIgnoreCase("Ana"))
                    .thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar("Ana", null, null, null, null);

            assertThat(result).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should search by apellido only when nombre not provided")
        void shouldSearchByApellidoOnlyWhenNombreNotProvided() {
            when(personaRepository.findByApellidoContainingIgnoreCase("Lopez"))
                    .thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar(null, "Lopez", null, null, null);

            assertThat(result).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should search by tipo identificacion when provided")
        void shouldSearchByTipoIdentificacionWhenProvided() {
            when(personaRepository.findByFkIdTipoIdentificacionIdTipoIdentificacion(1))
                    .thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar(null, null, null, 1, null);

            assertThat(result).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should search by esCliente when provided")
        void shouldSearchByEsClienteWhenProvided() {
            when(personaRepository.findByEsCliente(true)).thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar(null, null, null, null, true);

            assertThat(result).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should return empty list when all filters are null")
        void shouldReturnEmptyListWhenAllFiltersNull() {
            List<Persona> result = personaService.buscar(null, null, null, null, null);

            assertThat(result).isEmpty();
            verify(personaRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should deduplicate results from multiple filter matches")
        void shouldDeduplicateResultsFromMultipleFilters() {
            when(personaRepository.findByNombreContainingIgnoreCase("Ana"))
                    .thenReturn(List.of(testPersona));
            when(personaRepository.findByEsCliente(true)).thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar("Ana", null, null, null, true);

            assertThat(result).hasSize(1).containsExactly(testPersona);
        }

        @Test
        @DisplayName("Should ignore blank nombre")
        void shouldIgnoreBlankNombre() {
            when(personaRepository.findByApellidoContainingIgnoreCase("Lopez"))
                    .thenReturn(List.of(testPersona));

            List<Persona> result = personaService.buscar("  ", "Lopez", null, null, null);

            assertThat(result).containsExactly(testPersona);
            verify(personaRepository, never()).findByNombreContainingIgnoreCase(any());
        }
    }
}
