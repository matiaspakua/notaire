package com.licensis.notaire.unit;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.service.DeedService;
import com.licensis.notaire.service.NumeracionDeedService;
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

@DisplayName("EscrituraService unit tests")
@ExtendWith(MockitoExtension.class)
class DeedServiceTest {

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private NumeracionDeedService numeracionDeedService;

    @InjectMocks
    private DeedService deedService;

    private Deed testDeed;

    @BeforeEach
    void setUp() {
        testDeed = new Deed();
        testDeed.setIdDeed(1);
        testDeed.setNumber(100);
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all escrituras from repository")
        void shouldReturnAllEscrituras() {
            when(deedRepository.findAll()).thenReturn(List.of(testDeed));

            List<Deed> result = deedService.findAll();

            assertThat(result).hasSize(1).containsExactly(testDeed);
            verify(deedRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no escrituras exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(deedRepository.findAll()).thenReturn(Collections.emptyList());

            List<Deed> result = deedService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return deed when found by ID")
        void shouldReturnDeedWhenFound() {
            when(deedRepository.findById(1)).thenReturn(Optional.of(testDeed));

            Optional<Deed> result = deedService.findById(1);

            assertThat(result).isPresent().contains(testDeed);
        }

        @Test
        @DisplayName("Should return empty when deed not found")
        void shouldReturnEmptyWhenNotFound() {
            when(deedRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Deed> result = deedService.findById(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return deed")
        void shouldSaveDeed() {
            when(deedRepository.save(any(Deed.class))).thenReturn(testDeed);

            Deed result = deedService.save(testDeed);

            assertThat(result).isEqualTo(testDeed);
            verify(deedRepository).save(testDeed);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should call repository deleteById")
        void shouldDeleteDeed() {
            doNothing().when(deedRepository).deleteById(1);

            deedService.deleteById(1);

            verify(deedRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("findEscribanosDisponibles")
    class FindEscribanosDisponibles {

        @Test
        @DisplayName("Should return all escribanos from repository")
        void shouldReturnAllEscribanos() {
            Person notary = new Person();
            notary.setPersonId(1);
            notary.setFirstName("Juan");
            notary.setLastName("García");
            notary.setNotaryRegistrationNumber(1001);
            when(personRepository.findAllEscribanos()).thenReturn(List.of(notary));

            List<Person> result = deedService.findEscribanosDisponibles();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNotaryRegistrationNumber()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should return empty list when no escribanos exist")
        void shouldReturnEmptyListWhenNoEscribanos() {
            when(personRepository.findAllEscribanos()).thenReturn(Collections.emptyList());

            List<Person> result = deedService.findEscribanosDisponibles();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorNumero")
    class SearchPorNumber {

        @Test
        @DisplayName("Should return all escrituras when number is null")
        void shouldReturnAllWhenNumberIsNull() {
            when(deedRepository.findAll()).thenReturn(List.of(testDeed));

            List<Deed> result = deedService.searchPorNumber(null);

            assertThat(result).hasSize(1).containsExactly(testDeed);
            verify(deedRepository).findAll();
        }

        @Test
        @DisplayName("Should return matching deed by number")
        void shouldReturnMatchingDeedByNumber() {
            when(deedRepository.findByNumber(100)).thenReturn(Optional.of(testDeed));

            List<Deed> result = deedService.searchPorNumber(100);

            assertThat(result).hasSize(1).containsExactly(testDeed);
        }

        @Test
        @DisplayName("Should return empty list when no deed matches number")
        void shouldReturnEmptyListWhenNoMatch() {
            when(deedRepository.findByNumber(999)).thenReturn(Optional.empty());

            List<Deed> result = deedService.searchPorNumber(999);

            assertThat(result).isEmpty();
        }
    }
}
