package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import com.licensis.notaire.service.MovimientoTestimonioService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU11", "CU12", "CU44"})
@DisplayName("MovimientoTestimonioService Tests")
@ExtendWith(MockitoExtension.class)
class MovimientoTestimonioServiceTest {

    @Mock
    private MovimientoTestimonioRepository movimientoTestimonioRepository;

    @Mock
    private TestimonioRepository testimonioRepository;

    @InjectMocks
    private MovimientoTestimonioService movimientoTestimonioService;

    private Testimonio testimonio;

    @BeforeEach
    void setUp() {
        testimonio = new Testimonio();
        testimonio.setIdTestimonio(5);
        testimonio.setNumero(50);
        testimonio.setVerificado(true);
    }

    private MovimientoTestimonio movimientoConIngreso() {
        MovimientoTestimonio movimiento = new MovimientoTestimonio();
        movimiento.setIdMovimientoTestimonio(1);
        movimiento.setFechaIngreso(new Date());
        movimiento.setTestimonio(testimonio);
        return movimiento;
    }

    @Nested
    @DisplayName("Ingresar para inscripción")
    class IngresarInscripcionTests {

        @Test
        @DisplayName("Should register fecha de ingreso when testimonio is verified and has no open movement")
        void shouldRegisterIngresoInscripcion() {
            when(testimonioRepository.findById(5)).thenReturn(Optional.of(testimonio));
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.empty());
            when(movimientoTestimonioRepository.save(any(MovimientoTestimonio.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MovimientoTestimonio movimiento = movimientoTestimonioService.ingresarInscripcion(5);

            assertThat(movimiento.getFechaIngreso()).isNotNull();
            assertThat(movimiento.getTestimonio().getIdTestimonio()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject when testimonio already has an open movement without fecha de salida")
        void shouldRejectIngresoWhenAlreadyOpen() {
            MovimientoTestimonio abierto = movimientoConIngreso();
            when(testimonioRepository.findById(5)).thenReturn(Optional.of(testimonio));
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(abierto));

            assertThatThrownBy(() -> movimientoTestimonioService.ingresarInscripcion(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("trámite de inscripción");
        }

        @Test
        @DisplayName("Should reject when testimonio is not verified")
        void shouldRejectIngresoWhenTestimonioNotVerified() {
            testimonio.setVerificado(false);
            when(testimonioRepository.findById(5)).thenReturn(Optional.of(testimonio));

            assertThatThrownBy(() -> movimientoTestimonioService.ingresarInscripcion(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("verificado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimonio does not exist")
        void shouldRejectIngresoWhenTestimonioNotFound() {
            when(testimonioRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimientoTestimonioService.ingresarInscripcion(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registrar inscripción")
    class RegistrarInscripcionTests {

        @Test
        @DisplayName("Should mark as inscripto with fecha when testimonio was ingresado")
        void shouldRegisterInscripcion() {
            MovimientoTestimonio ingresado = movimientoConIngreso();
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(ingresado));
            when(movimientoTestimonioRepository.save(ingresado)).thenReturn(ingresado);

            MovimientoTestimonio resultado = movimientoTestimonioService.registrarInscripcion(5);

            assertThat(resultado.getInscripta()).isTrue();
            assertThat(resultado.getFechaInscripcion()).isNotNull();
        }

        @Test
        @DisplayName("Should reject when testimonio has no movement with fecha de ingreso")
        void shouldRejectInscripcionWithoutIngreso() {
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimientoTestimonioService.registrarInscripcion(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("ingreso");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimonio does not exist")
        void shouldRejectRegistrarInscripcionWhenTestimonioNotFound() {
            when(testimonioRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> movimientoTestimonioService.registrarInscripcion(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Retirar testimonio")
    class RetirarTests {

        @Test
        @DisplayName("Should register fecha de salida and numero de cartón when testimonio is inscripto")
        void shouldRegisterRetiro() {
            MovimientoTestimonio inscripto = movimientoConIngreso();
            inscripto.setInscripta(true);
            inscripto.setFechaInscripcion(new Date());
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(inscripto));
            when(movimientoTestimonioRepository.save(inscripto)).thenReturn(inscripto);

            MovimientoTestimonio resultado = movimientoTestimonioService.retirar(5, 123);

            assertThat(resultado.getFechaSalida()).isNotNull();
            assertThat(resultado.getNumeroCarton()).isEqualTo(123);
        }

        @Test
        @DisplayName("Should reject retiro when testimonio is not inscripto")
        void shouldRejectRetiroWhenNotInscripto() {
            MovimientoTestimonio ingresado = movimientoConIngreso();
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> movimientoTestimonioService.retirar(5, 123))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("inscripto");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimonio does not exist")
        void shouldRejectRetirarWhenTestimonioNotFound() {
            when(testimonioRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> movimientoTestimonioService.retirar(999, 123))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reingresar testimonio")
    class ReingresarTests {

        @Test
        @DisplayName("Should create a new movement preserving the previous one when testimonio was withdrawn")
        void shouldCreateNewMovementOnReingreso() {
            MovimientoTestimonio retirado = movimientoConIngreso();
            retirado.setInscripta(true);
            retirado.setFechaInscripcion(new Date());
            retirado.setFechaSalida(new Date());
            retirado.setNumeroCarton(123);

            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(retirado));
            when(movimientoTestimonioRepository.save(any(MovimientoTestimonio.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MovimientoTestimonio nuevo = movimientoTestimonioService.reingresar(5);

            assertThat(nuevo.getFechaIngreso()).isNotNull();
            assertThat(nuevo.getFechaSalida()).isNull();
            assertThat(nuevo).isNotSameAs(retirado);
            assertThat(retirado.getFechaSalida()).isNotNull();
        }

        @Test
        @DisplayName("Should reject reingreso when the most recent movement was not withdrawn")
        void shouldRejectReingresoWhenNotWithdrawn() {
            MovimientoTestimonio ingresado = movimientoConIngreso();
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> movimientoTestimonioService.reingresar(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should reject reingreso when testimonio has no previous movement")
        void shouldRejectReingresoWhenNoPreviousMovement() {
            when(testimonioRepository.existsById(5)).thenReturn(true);
            when(movimientoTestimonioRepository.findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> movimientoTestimonioService.reingresar(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimonio does not exist")
        void shouldRejectReingresarWhenTestimonioNotFound() {
            when(testimonioRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> movimientoTestimonioService.reingresar(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
