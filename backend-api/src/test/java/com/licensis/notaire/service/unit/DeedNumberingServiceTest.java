package com.licensis.notaire.service.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.DeedNumberingService;
import com.licensis.notaire.service.NumberingValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeedNumberingService Unit Tests (CU86)")
class DeedNumberingServiceTest {

    @Mock
    private FolioRepository folioRepository;

    @InjectMocks
    private DeedNumberingService deedNumberingService;

    private Person notary;

    @BeforeEach
    void setUp() {
        notary = new Person();
        notary.setPersonId(1);
        notary.setNotaryRegistrationNumber(100);
    }

    @Test
    @DisplayName("Should start correlativo at one when no deed exists yet in scope")
    void shouldStartCorrelativoAtOneWhenScopeIsEmpty() {
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, false, null))
                .thenReturn(Optional.empty());

        int siguiente = deedNumberingService.calculateNextSequenceNumber(notary, 2026, false);

        assertThat(siguiente).isEqualTo(1);
    }

    @Test
    @DisplayName("Should accept a número matching the expected correlativo")
    void shouldAcceptNumberMatchingExpectedCorrelativo() {
        when(folioRepository.existsNumberDeedByNotaryYearAndType(6, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        NumberingValidationResult resultado = deedNumberingService.validate(
                6, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(NumberingValidationResult.OK);
    }

    @Test
    @DisplayName("Should reject a número already used within the same scope")
    void shouldRejectDuplicateNumber() {
        when(folioRepository.existsNumberDeedByNotaryYearAndType(5, 1, 2026, false, null))
                .thenReturn(true);

        NumberingValidationResult resultado = deedNumberingService.validate(
                5, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(NumberingValidationResult.DUPLICATE);
    }

    @Test
    @DisplayName("Should require justificación when the número leaves a gap")
    void shouldRequireJustificationForGap() {
        when(folioRepository.existsNumberDeedByNotaryYearAndType(9, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        NumberingValidationResult resultado = deedNumberingService.validate(
                9, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(NumberingValidationResult.SKIP_UNJUSTIFIED);
    }

    @Test
    @DisplayName("Should accept a gap when a justificación is provided")
    void shouldAcceptGapWithJustification() {
        when(folioRepository.existsNumberDeedByNotaryYearAndType(9, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        NumberingValidationResult resultado = deedNumberingService.validate(
                9, notary, 2026, false, "Escritura anulada N° 6 a 8", null);

        assertThat(resultado).isEqualTo(NumberingValidationResult.SKIP_JUSTIFIED);
    }

    @Test
    @DisplayName("Should keep Protocolo Auxiliar numbering independent from Protocolo Principal")
    void shouldKeepAuxiliaryNumberingIndependentFromPrincipal() {
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, true, null))
                .thenReturn(Optional.of(2));
        when(folioRepository.findMaxNumberDeedByNotaryYearAndType(1, 2026, false, null))
                .thenReturn(Optional.of(40));

        int siguienteAuxiliary = deedNumberingService.calculateNextSequenceNumber(notary, 2026, true);
        int siguientePrincipal = deedNumberingService.calculateNextSequenceNumber(notary, 2026, false);

        assertThat(siguienteAuxiliary).isEqualTo(3);
        assertThat(siguientePrincipal).isEqualTo(41);
    }
}
