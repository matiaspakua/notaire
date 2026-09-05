package com.licensis.notaire.unit;

import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import com.licensis.notaire.service.EscrituraService;
import com.licensis.notaire.service.NumeracionEscrituraService;
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
class EscrituraServiceTest {

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private NumeracionEscrituraService numeracionEscrituraService;

    @InjectMocks
    private EscrituraService escrituraService;

    private Escritura testEscritura;

    @BeforeEach
    void setUp() {
        testEscritura = new Escritura();
        testEscritura.setIdEscritura(1);
        testEscritura.setNumero(100);
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all escrituras from repository")
        void shouldReturnAllEscrituras() {
            when(escrituraRepository.findAll()).thenReturn(List.of(testEscritura));

            List<Escritura> result = escrituraService.findAll();

            assertThat(result).hasSize(1).containsExactly(testEscritura);
            verify(escrituraRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no escrituras exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(escrituraRepository.findAll()).thenReturn(Collections.emptyList());

            List<Escritura> result = escrituraService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return escritura when found by ID")
        void shouldReturnEscrituraWhenFound() {
            when(escrituraRepository.findById(1)).thenReturn(Optional.of(testEscritura));

            Optional<Escritura> result = escrituraService.findById(1);

            assertThat(result).isPresent().contains(testEscritura);
        }

        @Test
        @DisplayName("Should return empty when escritura not found")
        void shouldReturnEmptyWhenNotFound() {
            when(escrituraRepository.findById(999)).thenReturn(Optional.empty());

            Optional<Escritura> result = escrituraService.findById(999);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return escritura")
        void shouldSaveEscritura() {
            when(escrituraRepository.save(any(Escritura.class))).thenReturn(testEscritura);

            Escritura result = escrituraService.save(testEscritura);

            assertThat(result).isEqualTo(testEscritura);
            verify(escrituraRepository).save(testEscritura);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should call repository deleteById")
        void shouldDeleteEscritura() {
            doNothing().when(escrituraRepository).deleteById(1);

            escrituraService.deleteById(1);

            verify(escrituraRepository).deleteById(1);
        }
    }

    @Nested
    @DisplayName("findEscribanosDisponibles")
    class FindEscribanosDisponibles {

        @Test
        @DisplayName("Should return all escribanos from repository")
        void shouldReturnAllEscribanos() {
            Persona escribano = new Persona();
            escribano.setIdPersona(1);
            escribano.setNombre("Juan");
            escribano.setApellido("García");
            escribano.setRegistroEscribano(1001);
            when(personaRepository.findAllEscribanos()).thenReturn(List.of(escribano));

            List<Persona> result = escrituraService.findEscribanosDisponibles();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRegistroEscribano()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should return empty list when no escribanos exist")
        void shouldReturnEmptyListWhenNoEscribanos() {
            when(personaRepository.findAllEscribanos()).thenReturn(Collections.emptyList());

            List<Persona> result = escrituraService.findEscribanosDisponibles();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarPorNumero")
    class BuscarPorNumero {

        @Test
        @DisplayName("Should return all escrituras when numero is null")
        void shouldReturnAllWhenNumeroIsNull() {
            when(escrituraRepository.findAll()).thenReturn(List.of(testEscritura));

            List<Escritura> result = escrituraService.buscarPorNumero(null);

            assertThat(result).hasSize(1).containsExactly(testEscritura);
            verify(escrituraRepository).findAll();
        }

        @Test
        @DisplayName("Should return matching escritura by numero")
        void shouldReturnMatchingEscrituraByNumero() {
            when(escrituraRepository.findByNumero(100)).thenReturn(Optional.of(testEscritura));

            List<Escritura> result = escrituraService.buscarPorNumero(100);

            assertThat(result).hasSize(1).containsExactly(testEscritura);
        }

        @Test
        @DisplayName("Should return empty list when no escritura matches numero")
        void shouldReturnEmptyListWhenNoMatch() {
            when(escrituraRepository.findByNumero(999)).thenReturn(Optional.empty());

            List<Escritura> result = escrituraService.buscarPorNumero(999);

            assertThat(result).isEmpty();
        }
    }
}
