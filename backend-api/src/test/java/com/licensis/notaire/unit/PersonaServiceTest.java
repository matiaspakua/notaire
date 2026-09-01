package com.licensis.notaire.unit;

import com.licensis.notaire.exception.PersonaDuplicadaException;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU17", "CU18", "CU41", "CU54", "CU61"})
@DisplayName("PersonaService unit tests")
@ExtendWith(MockitoExtension.class)
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
        testPersona.setNombre("Ana");
        testPersona.setApellido("Lopez");
        testPersona.setNumeroIdentificacion("12345678");
        testPersona.setEsCliente(true);
        testPersona.setFkIdTipoIdentificacion(tipoIdentificacion);
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

        @Test
        @DisplayName("Should create persona when document is not registered")
        void shouldCreatePersonaWhenDocumentNotRegistered() {
            Persona nueva = new Persona();
            nueva.setNumeroIdentificacion("87654321");
            nueva.setFkIdTipoIdentificacion(tipoIdentificacion);
            when(personaRepository.findByNumeroIdentificacion("87654321")).thenReturn(Optional.empty());
            when(personaRepository.save(nueva)).thenReturn(nueva);

            Persona result = personaService.save(nueva);

            assertThat(result).isEqualTo(nueva);
            verify(personaRepository).save(nueva);
        }

        @Test
        @DisplayName("Should reject create when document is already registered")
        void shouldRejectCreateWhenDocumentAlreadyRegistered() {
            Persona nueva = new Persona();
            nueva.setNumeroIdentificacion("12345678");
            nueva.setFkIdTipoIdentificacion(tipoIdentificacion);
            when(personaRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPersona));

            assertThatThrownBy(() -> personaService.save(nueva))
                    .isInstanceOf(PersonaDuplicadaException.class)
                    .extracting(ex -> ((PersonaDuplicadaException) ex).getIdPersonaExistente())
                    .isEqualTo(1);

            verify(personaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update persona without changing the document")
        void shouldUpdatePersonaWithoutChangingDocument() {
            when(personaRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPersona));
            when(personaRepository.save(testPersona)).thenReturn(testPersona);

            Persona result = personaService.save(testPersona);

            assertThat(result).isEqualTo(testPersona);
            verify(personaRepository).save(testPersona);
        }

        @Test
        @DisplayName("Should reject update when document belongs to another persona")
        void shouldRejectUpdateWhenDocumentBelongsToAnotherPersona() {
            Persona editada = new Persona();
            editada.setIdPersona(2);
            editada.setNumeroIdentificacion("12345678");
            editada.setFkIdTipoIdentificacion(tipoIdentificacion);
            when(personaRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPersona));

            assertThatThrownBy(() -> personaService.save(editada))
                    .isInstanceOf(PersonaDuplicadaException.class);

            verify(personaRepository, never()).save(any());
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
