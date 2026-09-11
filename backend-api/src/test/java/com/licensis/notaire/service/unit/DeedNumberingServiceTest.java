package com.licensis.notaire.service.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.NumeracionDeedService;
import com.licensis.notaire.service.ResultadoValidacionNumeracion;
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
@DisplayName("NumeracionEscrituraService Unit Tests (CU86)")
class NumeracionDeedServiceTest {

    @Mock
    private FolioRepository folioRepository;

    @InjectMocks
    private NumeracionDeedService numeracionDeedService;

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
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, false, null))
                .thenReturn(Optional.empty());

        int siguiente = numeracionDeedService.calcularSiguienteCorrelativo(notary, 2026, false);

        assertThat(siguiente).isEqualTo(1);
    }

    @Test
    @DisplayName("Should accept a número matching the expected correlativo")
    void shouldAcceptNumberMatchingExpectedCorrelativo() {
        when(folioRepository.existsNumberDeedByNotaryYearYType(6, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        ResultadoValidacionNumeracion resultado = numeracionDeedService.validar(
                6, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(ResultadoValidacionNumeracion.OK);
    }

    @Test
    @DisplayName("Should reject a número already used within the same scope")
    void shouldRejectDuplicateNumber() {
        when(folioRepository.existsNumberDeedByNotaryYearYType(5, 1, 2026, false, null))
                .thenReturn(true);

        ResultadoValidacionNumeracion resultado = numeracionDeedService.validar(
                5, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(ResultadoValidacionNumeracion.DUPLICADO);
    }

    @Test
    @DisplayName("Should require justificación when the número leaves a gap")
    void shouldRequireJustificationForGap() {
        when(folioRepository.existsNumberDeedByNotaryYearYType(9, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        ResultadoValidacionNumeracion resultado = numeracionDeedService.validar(
                9, notary, 2026, false, null, null);

        assertThat(resultado).isEqualTo(ResultadoValidacionNumeracion.SALTO_SIN_JUSTIFICAR);
    }

    @Test
    @DisplayName("Should accept a gap when a justificación is provided")
    void shouldAcceptGapWithJustification() {
        when(folioRepository.existsNumberDeedByNotaryYearYType(9, 1, 2026, false, null))
                .thenReturn(false);
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, false, null))
                .thenReturn(Optional.of(5));

        ResultadoValidacionNumeracion resultado = numeracionDeedService.validar(
                9, notary, 2026, false, "Escritura anulada N° 6 a 8", null);

        assertThat(resultado).isEqualTo(ResultadoValidacionNumeracion.SALTO_JUSTIFICADO);
    }

    @Test
    @DisplayName("Should keep Protocolo Auxiliar numbering independent from Protocolo Principal")
    void shouldKeepAuxiliaryNumberingIndependentFromPrincipal() {
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, true, null))
                .thenReturn(Optional.of(2));
        when(folioRepository.findMaxNumberDeedByNotaryYearYType(1, 2026, false, null))
                .thenReturn(Optional.of(40));

        int siguienteAuxiliary = numeracionDeedService.calcularSiguienteCorrelativo(notary, 2026, true);
        int siguientePrincipal = numeracionDeedService.calcularSiguienteCorrelativo(notary, 2026, false);

        assertThat(siguienteAuxiliary).isEqualTo(3);
        assertThat(siguientePrincipal).isEqualTo(41);
    }
}
