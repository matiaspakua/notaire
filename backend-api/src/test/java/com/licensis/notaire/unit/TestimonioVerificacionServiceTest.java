package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import com.licensis.notaire.service.TestimonioGeneracionVerificacionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU08"})
@DisplayName("TestimonioGeneracionVerificacionService - Verificación Tests")
@ExtendWith(MockitoExtension.class)
class TestimonioVerificacionServiceTest {

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private TestimonioRepository testimonioRepository;

    @InjectMocks
    private TestimonioGeneracionVerificacionService testimonioService;

    private Testimonio testimonio;

    @BeforeEach
    void setUp() {
        testimonio = new Testimonio();
        testimonio.setIdTestimonio(5);
        testimonio.setNumero(50);
    }

    @Test
    @DisplayName("Should verify testimonio without observations")
    void shouldVerifyWithoutObservations() {
        when(testimonioRepository.findById(5)).thenReturn(Optional.of(testimonio));
        when(testimonioRepository.save(testimonio)).thenReturn(testimonio);

        Testimonio verificado = testimonioService.verificar(5, false, null);

        assertThat(verificado.getVerificado()).isTrue();
        assertThat(verificado.getObservado()).isFalse();
        assertThat(verificado.getObservaciones()).isNull();
    }

    @Test
    @DisplayName("Should verify testimonio with observations")
    void shouldVerifyWithObservations() {
        when(testimonioRepository.findById(5)).thenReturn(Optional.of(testimonio));
        when(testimonioRepository.save(testimonio)).thenReturn(testimonio);

        Testimonio verificado = testimonioService.verificar(5, true, "Falta una firma");

        assertThat(verificado.getVerificado()).isTrue();
        assertThat(verificado.getObservado()).isTrue();
        assertThat(verificado.getObservaciones()).isEqualTo("Falta una firma");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when testimonio does not exist")
    void shouldRejectVerificationWhenTestimonioNotFound() {
        when(testimonioRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testimonioService.verificar(999, false, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
