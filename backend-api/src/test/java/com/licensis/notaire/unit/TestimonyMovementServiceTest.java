package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.application.usecase.testimony.TestimonyMovementService;
import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
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
@DisplayName("TestimonyMovementService Tests")
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
    @DisplayName("Enter for registration")
    class EnterRegistrationTests {

        @Test
        @DisplayName("Should register the entry date when the testimony is verified and has no open movement")
        void shouldRegisterEntryRegistration() {
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());
            when(testimonyMovementRepository.save(any(TestimonyMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TestimonyMovement movement = testimonyMovementService.registerEntry(5);

            assertThat(movement.getDateEntry()).isNotNull();
            assertThat(movement.getTestimony().getIdTestimony()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject when the testimony already has an open movement without an exit date")
        void shouldRejectEntryWhenAlreadyOpen() {
            TestimonyMovement abierto = movementConEntry();
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(abierto));

            assertThatThrownBy(() -> testimonyMovementService.registerEntry(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("procedure of registration");
        }

        @Test
        @DisplayName("Should reject when testimony is not verified")
        void shouldRejectEntryWhenTestimonyNotVerified() {
            testimony.setVerified(false);
            when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));

            assertThatThrownBy(() -> testimonyMovementService.registerEntry(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("verified");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectEntryWhenTestimonyNotFound() {
            when(testimonyRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.registerEntry(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Record registration")
    class RegisterRegistrationTests {

        @Test
        @DisplayName("Should mark the testimony inscripto with a date when it was ingresado")
        void shouldRegisterRegistration() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));
            when(testimonyMovementRepository.save(ingresado)).thenReturn(ingresado);

            TestimonyMovement result = testimonyMovementService.registerInscription(5);

            assertThat(result.getRegistered()).isTrue();
            assertThat(result.getDateRegistration()).isNotNull();
        }

        @Test
        @DisplayName("Should reject when the testimony has no movement with an entry date")
        void shouldRejectRegistrationWithoutEntry() {
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.registerInscription(5))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("ingreso");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectRegistrarRegistrationWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.registerInscription(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Withdraw testimony")
    class WithdrawTests {

        @Test
        @DisplayName("Should register the exit date and carton number when the testimony is registered")
        void shouldRegisterWithdrawal() {
            TestimonyMovement inscripto = movementConEntry();
            inscripto.setRegistered(true);
            inscripto.setDateRegistration(new Date());
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(inscripto));
            when(testimonyMovementRepository.save(inscripto)).thenReturn(inscripto);

            TestimonyMovement result = testimonyMovementService.withdraw(5, 123);

            assertThat(result.getDateExit()).isNotNull();
            assertThat(result.getCardNumber()).isEqualTo(123);
        }

        @Test
        @DisplayName("Should reject withdrawal when the testimony is not inscripto")
        void shouldRejectWithdrawalWhenNotInscripto() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> testimonyMovementService.withdraw(5, 123))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("registered");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectRetirarWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.withdraw(999, 123))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Re-enter testimony")
    class ReenterTests {

        @Test
        @DisplayName("Should create a new movement preserving the previous one when testimony was withdrawn")
        void shouldCreateNewMovementOnReentry() {
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

            TestimonyMovement nuevo = testimonyMovementService.reenter(5, TestimonyReentry.empty());

            assertThat(nuevo.getDateEntry()).isNotNull();
            assertThat(nuevo.getDateExit()).isNull();
            assertThat(nuevo).isNotSameAs(retirado);
            assertThat(retirado.getDateExit()).isNotNull();
        }

        @Test
        @DisplayName("Should store the carton number, observation flag, and notes on the new movement")
        void shouldStoreReentryDataOnTheNewMovement() {
            TestimonyMovement retirado = movementConEntry();
            retirado.setDateExit(new Date());
            retirado.setCardNumber(123);

            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(retirado));
            when(testimonyMovementRepository.save(any(TestimonyMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TestimonyMovement nuevo = testimonyMovementService.reenter(5,
                    new TestimonyReentry(77, true, "Falta firma of the notary"));

            assertThat(nuevo.getCardNumber()).isEqualTo(77);
            assertThat(nuevo.isObservedByRegistry()).isTrue();
            assertThat(nuevo.getNotes()).isEqualTo("Falta firma of the notary");
            assertThat(retirado.getCardNumber()).isEqualTo(123);
            assertThat(retirado.isObservedByRegistry()).isFalse();
        }

        @Test
        @DisplayName("Should default to card 0, not observed and no notes when no data is given")
        void shouldDefaultReentryDataWhenEmpty() {
            TestimonyMovement retirado = movementConEntry();
            retirado.setDateExit(new Date());

            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(retirado));
            when(testimonyMovementRepository.save(any(TestimonyMovement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TestimonyMovement nuevo = testimonyMovementService.reenter(5, TestimonyReentry.empty());

            assertThat(nuevo.getCardNumber()).isZero();
            assertThat(nuevo.isObservedByRegistry()).isFalse();
            assertThat(nuevo.getNotes()).isNull();
        }

        @Test
        @DisplayName("Should reject an observed reentry without notes")
        void shouldRejectObservedReentryWithoutNotes() {
            TestimonyMovement retirado = movementConEntry();
            retirado.setDateExit(new Date());

            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(retirado));

            assertThatThrownBy(() -> testimonyMovementService.reenter(5, new TestimonyReentry(77, true, "  ")))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("notes");
        }

        @Test
        @DisplayName("Should reject reentry when the most recent movement was not withdrawn")
        void shouldRejectReentryWhenNotWithdrawn() {
            TestimonyMovement ingresado = movementConEntry();
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.of(ingresado));

            assertThatThrownBy(() -> testimonyMovementService.reenter(5, TestimonyReentry.empty()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should reject reentry when the testimony has no previous movement")
        void shouldRejectReentryWhenNoPreviousMovement() {
            when(testimonyRepository.existsById(5)).thenReturn(true);
            when(testimonyMovementRepository.findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(5))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testimonyMovementService.reenter(5, TestimonyReentry.empty()))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("retirado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
        void shouldRejectReenterWhenTestimonyNotFound() {
            when(testimonyRepository.existsById(999)).thenReturn(false);

            assertThatThrownBy(() -> testimonyMovementService.reenter(999, TestimonyReentry.empty()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
