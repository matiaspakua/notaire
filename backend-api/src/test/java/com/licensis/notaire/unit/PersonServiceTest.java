package com.licensis.notaire.unit;

import com.licensis.notaire.exception.DuplicatePersonException;
import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.TipoIdentificacion;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.service.PersonService;
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
@DisplayName("PersonService unit tests")
@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person testPerson;
    private TipoIdentificacion tipoIdentificacion;

    @BeforeEach
    void setUp() {
        tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setIdTipoIdentificacion(1);
        tipoIdentificacion.setNombre("DNI");

        testPerson = new Person();
        testPerson.setPersonId(1);
        testPerson.setFirstName("Ana");
        testPerson.setLastName("Lopez");
        testPerson.setIdentificationNumber("12345678");
        testPerson.setIsClient(true);
        testPerson.setFkIdIdentificationType(tipoIdentificacion);
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all people")
        void shouldReturnAllPeople() {
            when(personRepository.findAll()).thenReturn(List.of(testPerson));

            List<Person> result = personService.findAll();

            assertThat(result).hasSize(1).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should return empty list when no people exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(personRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(personService.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return person when found")
        void shouldReturnPersonWhenFound() {
            when(personRepository.findById(1)).thenReturn(Optional.of(testPerson));

            assertThat(personService.findById(1)).isPresent().contains(testPerson);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            when(personRepository.findById(999)).thenReturn(Optional.empty());

            assertThat(personService.findById(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return the person")
        void shouldSaveAndReturnPerson() {
            when(personRepository.save(any(Person.class))).thenReturn(testPerson);

            Person result = personService.save(testPerson);

            assertThat(result).isEqualTo(testPerson);
            verify(personRepository).save(testPerson);
        }

        @Test
        @DisplayName("Should create person when document is not registered")
        void shouldCreatePersonWhenDocumentNotRegistered() {
            Person newPerson = new Person();
            newPerson.setIdentificationNumber("87654321");
            newPerson.setFkIdIdentificationType(tipoIdentificacion);
            when(personRepository.findByNumeroIdentificacion("87654321")).thenReturn(Optional.empty());
            when(personRepository.save(newPerson)).thenReturn(newPerson);

            Person result = personService.save(newPerson);

            assertThat(result).isEqualTo(newPerson);
            verify(personRepository).save(newPerson);
        }

        @Test
        @DisplayName("Should reject create when document is already registered")
        void shouldRejectCreateWhenDocumentAlreadyRegistered() {
            Person newPerson = new Person();
            newPerson.setIdentificationNumber("12345678");
            newPerson.setFkIdIdentificationType(tipoIdentificacion);
            when(personRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPerson));

            assertThatThrownBy(() -> personService.save(newPerson))
                    .isInstanceOf(DuplicatePersonException.class)
                    .extracting(ex -> ((DuplicatePersonException) ex).getIdPersonaExistente())
                    .isEqualTo(1);

            verify(personRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update person without changing the document")
        void shouldUpdatePersonWithoutChangingDocument() {
            when(personRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPerson));
            when(personRepository.save(testPerson)).thenReturn(testPerson);

            Person result = personService.save(testPerson);

            assertThat(result).isEqualTo(testPerson);
            verify(personRepository).save(testPerson);
        }

        @Test
        @DisplayName("Should reject update when document belongs to another person")
        void shouldRejectUpdateWhenDocumentBelongsToAnotherPerson() {
            Person edited = new Person();
            edited.setPersonId(2);
            edited.setIdentificationNumber("12345678");
            edited.setFkIdIdentificationType(tipoIdentificacion);
            when(personRepository.findByNumeroIdentificacion("12345678")).thenReturn(Optional.of(testPerson));

            assertThatThrownBy(() -> personService.save(edited))
                    .isInstanceOf(DuplicatePersonException.class);

            verify(personRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should delegate to repository deleteById")
        void shouldDeletePerson() {
            doNothing().when(personRepository).deleteById(1);

            personService.deleteById(1);

            verify(personRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("search - CU18/CU41/CU54/CU61")
    class Search {

        @Test
        @DisplayName("Should search by identificationNumber when provided")
        void shouldSearchByIdentificationNumberWhenProvided() {
            when(personRepository.findByNumeroIdentificacion("12345678"))
                    .thenReturn(Optional.of(testPerson));

            List<Person> result = personService.search(null, null, "12345678", null, null);

            assertThat(result).hasSize(1).containsExactly(testPerson);
            verify(personRepository).findByNumeroIdentificacion("12345678");
            verify(personRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should return empty list when identificationNumber not found")
        void shouldReturnEmptyWhenIdentificationNumberNotFound() {
            when(personRepository.findByNumeroIdentificacion("99999999")).thenReturn(Optional.empty());

            List<Person> result = personService.search(null, null, "99999999", null, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should search by firstName and lastName when both provided")
        void shouldSearchByFirstNameAndLastNameWhenBothProvided() {
            when(personRepository.findByNombreAndApellidoContainingIgnoreCase("Ana", "Lopez"))
                    .thenReturn(List.of(testPerson));

            List<Person> result = personService.search("Ana", "Lopez", null, null, null);

            assertThat(result).hasSize(1).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should search by firstName only when lastName not provided")
        void shouldSearchByFirstNameOnlyWhenLastNameNotProvided() {
            when(personRepository.findByNombreContainingIgnoreCase("Ana"))
                    .thenReturn(List.of(testPerson));

            List<Person> result = personService.search("Ana", null, null, null, null);

            assertThat(result).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should search by lastName only when firstName not provided")
        void shouldSearchByLastNameOnlyWhenFirstNameNotProvided() {
            when(personRepository.findByApellidoContainingIgnoreCase("Lopez"))
                    .thenReturn(List.of(testPerson));

            List<Person> result = personService.search(null, "Lopez", null, null, null);

            assertThat(result).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should search by identification type when provided")
        void shouldSearchByIdentificationTypeWhenProvided() {
            when(personRepository.findByFkIdTipoIdentificacionIdTipoIdentificacion(1))
                    .thenReturn(List.of(testPerson));

            List<Person> result = personService.search(null, null, null, 1, null);

            assertThat(result).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should search by isClient when provided")
        void shouldSearchByIsClientWhenProvided() {
            when(personRepository.findByEsCliente(true)).thenReturn(List.of(testPerson));

            List<Person> result = personService.search(null, null, null, null, true);

            assertThat(result).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should return empty list when all filters are null")
        void shouldReturnEmptyListWhenAllFiltersNull() {
            List<Person> result = personService.search(null, null, null, null, null);

            assertThat(result).isEmpty();
            verify(personRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should deduplicate results from multiple filter matches")
        void shouldDeduplicateResultsFromMultipleFilters() {
            when(personRepository.findByNombreContainingIgnoreCase("Ana"))
                    .thenReturn(List.of(testPerson));
            when(personRepository.findByEsCliente(true)).thenReturn(List.of(testPerson));

            List<Person> result = personService.search("Ana", null, null, null, true);

            assertThat(result).hasSize(1).containsExactly(testPerson);
        }

        @Test
        @DisplayName("Should ignore blank firstName")
        void shouldIgnoreBlankFirstName() {
            when(personRepository.findByApellidoContainingIgnoreCase("Lopez"))
                    .thenReturn(List.of(testPerson));

            List<Person> result = personService.search("  ", "Lopez", null, null, null);

            assertThat(result).containsExactly(testPerson);
            verify(personRepository, never()).findByNombreContainingIgnoreCase(any());
        }
    }
}
