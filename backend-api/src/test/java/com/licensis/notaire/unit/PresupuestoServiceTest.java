package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.PresupuestoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PresupuestoService unit tests")
@ExtendWith(MockitoExtension.class)
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
        testPresupuesto.setNumero(1001);
        testPresupuesto.setEncabezado("Presupuesto test");
        testPresupuesto.setEstado("PENDIENTE");
        testPresupuesto.setFecha(new Date());
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all presupuestos")
        void shouldReturnAllPresupuestos() {
            when(presupuestoRepository.findAll()).thenReturn(List.of(testPresupuesto));

            List<Presupuesto> result = presupuestoService.findAll();

            assertThat(result).hasSize(1).containsExactly(testPresupuesto);
            verify(presupuestoRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no presupuestos exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(presupuestoRepository.findAll()).thenReturn(List.of());

            assertThat(presupuestoService.findAll()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return presupuesto when found")
        void shouldReturnPresupuestoWhenFound() {
            when(presupuestoRepository.findById(1)).thenReturn(Optional.of(testPresupuesto));

            assertThat(presupuestoService.findById(1)).isPresent().contains(testPresupuesto);
        }

        @Test
        @DisplayName("Should return empty when presupuesto not found")
        void shouldReturnEmptyWhenNotFound() {
            when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

            assertThat(presupuestoService.findById(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPersona - CU60")
    class FindByPersona {

        @Test
        @DisplayName("Should return presupuestos for a given persona")
        void shouldReturnPresupuestosForPersona() {
            when(presupuestoRepository.findByFkIdPersonaIdPersona(5)).thenReturn(List.of(testPresupuesto));

            List<Presupuesto> result = presupuestoService.findByPersona(5);

            assertThat(result).hasSize(1).containsExactly(testPresupuesto);
        }

        @Test
        @DisplayName("Should return empty list when persona has no presupuestos")
        void shouldReturnEmptyWhenPersonaHasNoPresupuestos() {
            when(presupuestoRepository.findByFkIdPersonaIdPersona(99)).thenReturn(List.of());

            assertThat(presupuestoService.findByPersona(99)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEstado - CU60")
    class FindByEstado {

        @Test
        @DisplayName("Should return presupuestos filtered by estado")
        void shouldFilterByEstado() {
            when(presupuestoRepository.findByEstado("PENDIENTE")).thenReturn(List.of(testPresupuesto));

            List<Presupuesto> result = presupuestoService.findByEstado("PENDIENTE");

            assertThat(result).hasSize(1).containsExactly(testPresupuesto);
        }

        @Test
        @DisplayName("Should return all presupuestos when estado is null")
        void shouldReturnAllWhenEstadoIsNull() {
            when(presupuestoRepository.findAll()).thenReturn(List.of(testPresupuesto));

            List<Presupuesto> result = presupuestoService.findByEstado(null);

            assertThat(result).hasSize(1);
            verify(presupuestoRepository).findAll();
        }

        @Test
        @DisplayName("Should return all presupuestos when estado is blank")
        void shouldReturnAllWhenEstadoIsBlank() {
            when(presupuestoRepository.findAll()).thenReturn(List.of(testPresupuesto));

            List<Presupuesto> result = presupuestoService.findByEstado("   ");

            assertThat(result).hasSize(1);
            verify(presupuestoRepository).findAll();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Should save presupuesto and return it with generated ID")
        void shouldCreatePresupuesto() {
            when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(testPresupuesto);

            Presupuesto result = presupuestoService.create(testPresupuesto);

            assertThat(result).isNotNull();
            assertThat(result.getIdPresupuesto()).isEqualTo(1);
            verify(presupuestoRepository).save(testPresupuesto);
        }

        @Test
        @DisplayName("Should set default encabezado when blank")
        void shouldSetDefaultEncabezadoWhenBlank() {
            Presupuesto p = new Presupuesto();
            p.setNumero(100);
            p.setEstado("PENDIENTE");
            p.setFecha(new Date());
            when(presupuestoRepository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

            Presupuesto result = presupuestoService.create(p);

            assertThat(result.getEncabezado()).isEqualTo("Presupuesto");
        }

        @Test
        @DisplayName("Should generate numero when zero")
        void shouldGenerateNumeroWhenZero() {
            Presupuesto p = new Presupuesto();
            p.setEncabezado("Test");
            p.setEstado("PENDIENTE");
            p.setFecha(new Date());
            p.setNumero(0);
            when(presupuestoRepository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

            Presupuesto result = presupuestoService.create(p);

            assertThat(result.getNumero()).isNotZero();
        }

        @Test
        @DisplayName("Should keep provided encabezado when not blank")
        void shouldKeepProvidedEncabezado() {
            testPresupuesto.setEncabezado("Presupuesto específico");
            when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(testPresupuesto);

            Presupuesto result = presupuestoService.create(testPresupuesto);

            assertThat(result.getEncabezado()).isEqualTo("Presupuesto específico");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Should update and return presupuesto when it exists")
        void shouldUpdateWhenExists() {
            when(presupuestoRepository.existsById(1)).thenReturn(true);
            when(presupuestoRepository.save(any(Presupuesto.class))).thenReturn(testPresupuesto);

            Presupuesto result = presupuestoService.update(1, testPresupuesto);

            assertThat(result).isNotNull();
            assertThat(result.getIdPresupuesto()).isEqualTo(1);
            verify(presupuestoRepository).save(testPresupuesto);
        }

        @Test
        @DisplayName("Should set the ID from path variable before saving")
        void shouldSetIdBeforeSaving() {
            Presupuesto incoming = new Presupuesto();
            incoming.setEstado("APROBADO");
            when(presupuestoRepository.existsById(1)).thenReturn(true);
            when(presupuestoRepository.save(any(Presupuesto.class))).thenAnswer(inv -> inv.getArgument(0));

            Presupuesto result = presupuestoService.update(1, incoming);

            assertThat(result.getIdPresupuesto()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when presupuesto does not exist")
        void shouldThrowWhenPresupuestoNotFound() {
            when(presupuestoRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> presupuestoService.update(999, testPresupuesto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Should delete presupuesto when it exists")
        void shouldDeleteWhenExists() {
            when(presupuestoRepository.existsById(1)).thenReturn(true);
            doNothing().when(presupuestoRepository).deleteById(1);

            presupuestoService.deleteById(1);

            verify(presupuestoRepository).deleteById(1);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when presupuesto does not exist")
        void shouldThrowWhenPresupuestoNotFound() {
            when(presupuestoRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> presupuestoService.deleteById(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }
}
