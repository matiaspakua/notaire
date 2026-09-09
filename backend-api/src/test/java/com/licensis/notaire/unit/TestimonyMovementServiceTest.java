package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.service.TestimonyMovementService;
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
class TestimonyMovementServiceTest {

    @Mock
    private TestimonyMovementRepository testimonyMovementRepository;

    @Mock
    private TestimonyRepository testimonyRepository;

    @InjectMocks
    private TestimonyMovementService testimonyMovementService;

    private Testimony testimony;

    @BeforeEach
    void setUp() {
        testimony = new Testimony();
        testimony.setIdTestimony(5);
        testimony.setNumber(50);
        testimony.setVerified(true);
    }

    private TestimonyMovement movementConEntry() {
        TestimonyMovement movement = new TestimonyMovement();
        movement.setIdTestimonyMovement(1);
        movement.setDateEntry(new Date());
        movement.setTestimony(testimony);
        return movement;
    }

    @Nested
    @DisplayName("Ingresar para inscripción")
    class IngresarRegistrationTests {

        @Test
        @DisplayName("Should register date de ingreso when testimony is verified and has no open movement")
        void shouldRegisterEntryRegistration() {
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());
            when(testimonyMovementRepository.save(any(TestimonyMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TestimonyMovement movement = testimonyMovementService.ingresarRegistration(5);

            assertThat(movement.getDateEntry()).isNotNull();
            assertThat(movement.getTestimony().getIdTestimony()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject when testimony already has an open movement without date de salida")
        void shouldRejectEntryWhenAlreadyOpen() {
            TestimonyMovement abierto = movementConEntry();
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(abierto));

            assertThatThrownBy(() -> testimonyMovementService.ingresarRegistration(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("trámite de inscripción");
        }

        @Test
        @DisplayName("Should reject when testimony is not verified")
        void shouldRejectEntryWhenTestimonyNotVerified() {
            testimony.setVerified(false);
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));

            assertThatThrownBy(() -> testimonyMovementService.ingresarRegistration(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("verificado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectEntryWhenTestimonyNotFound() {
            when(testimonyRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.ingresarRegistration(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registrar inscripción")
    class RegistrarRegistrationTests {

        @Test
        @DisplayName("Should mark as inscripto with date when testimony was ingresado")
        void shouldRegisterRegistration() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));
            when(testimonyMovementRepository.save(ingresado)).thenReturn(ingresado);

            TestimonyMovement resultado = testimonyMovementService.registrarRegistration(5);

            assertThat(resultado.getRegistered()).isTrue();
            assertThat(resultado.getDateRegistration()).isNotNull();
        }

        @Test
        @DisplayName("Should reject when testimony has no movement with date de ingreso")
        void shouldRejectRegistrationWithoutEntry() {
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.registrarRegistration(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("ingreso");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectRegistrarRegistrationWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.registrarRegistration(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Retirar testimony")
    class RetirarTests {

        @Test
        @DisplayName("Should register date de salida and number de cartón when testimony is inscripto")
        void shouldRegisterWithdrawal() {
            TestimonyMovement inscripto = movementConEntry();
            inscripto.setRegistered(true);
            inscripto.setDateRegistration(new Date());
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(inscripto));
            when(testimonyMovementRepository.save(inscripto)).thenReturn(inscripto);

            TestimonyMovement resultado = testimonyMovementService.retirar(5, 123);

            assertThat(resultado.getDateExit()).isNotNull();
            assertThat(resultado.getCardNumber()).isEqualTo(123);
        }

        @Test
        @DisplayName("Should reject retiro when testimony is not inscripto")
        void shouldRejectWithdrawalWhenNotInscripto() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> testimonyMovementService.retirar(5, 123))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("inscripto");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectRetirarWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.retirar(999, 123))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reingresar testimony")
    class ReingresarTests {

        @Test
        @DisplayName("Should create a new movement preserving the previous one when testimony was withdrawn")
        void shouldCreateNewMovementOnReingreso() {
            TestimonyMovement retirado = movementConEntry();
            retirado.setRegistered(true);
            retirado.setDateRegistration(new Date());
            retirado.setDateExit(new Date());
            retirado.setCardNumber(123);

            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(retirado));
            when(testimonyMovementRepository.save(any(TestimonyMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TestimonyMovement nuevo = testimonyMovementService.reingresar(5);

            assertThat(nuevo.getDateEntry()).isNotNull();
            assertThat(nuevo.getDateExit()).isNull();
            assertThat(nuevo).isNotSameAs(retirado);
            assertThat(retirado.getDateExit()).isNotNull();
        }

        @Test
        @DisplayName("Should reject reingreso when the most recent movement was not withdrawn")
        void shouldRejectReingresoWhenNotWithdrawn() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> testimonyMovementService.reingresar(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should reject reingreso when testimony has no previous movement")
        void shouldRejectReingresoWhenNoPreviousMovement() {
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.reingresar(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectReingresarWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.reingresar(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
