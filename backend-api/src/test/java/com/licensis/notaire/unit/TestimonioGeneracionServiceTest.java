package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import com.licensis.notaire.service.TestimonioGeneracionVerificacionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU07"})
@DisplayName("TestimonioGeneracionVerificacionService - Generación Tests")
@ExtendWith(MockitoExtension.class)
class TestimonioGeneracionServiceTest {

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private TestimonioRepository testimonioRepository;

    @InjectMocks
    private TestimonioGeneracionVerificacionService testimonioService;

    private Escritura escritura;

    @BeforeEach
    void setUp() {
        escritura = new Escritura();
        escritura.setIdEscritura(1);
        escritura.setNumero(100);
        escritura.setEstado(ConstantesNegocio.ESCRITURA_FIRMADA);
    }

    @Test
    @DisplayName("Should generate testimonio from a signed escritura")
    void shouldGenerateTestimonioFromSignedEscritura() {
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));
        when(testimonioRepository.save(org.mockito.ArgumentMatchers.any(Testimonio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Testimonio generado = testimonioService.generar(1);

        ArgumentCaptor<Testimonio> captor = ArgumentCaptor.forClass(Testimonio.class);
        verify(testimonioRepository).save(captor.capture());

        assertThat(generado).isNotNull();
        assertThat(generado.getFkIdEscritura().getIdEscritura()).isEqualTo(1);
        assertThat(generado.getVerificado()).isFalse();
        assertThat(captor.getValue().getFkIdEscritura().getIdEscritura()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject generation when escritura is not signed")
    void shouldRejectGenerationWhenEscrituraNotSigned() {
        escritura.setEstado(ConstantesNegocio.ESCRITURA_SIN_FIRMAR);
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));

        assertThatThrownBy(() -> testimonioService.generar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Firmada");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when escritura does not exist")
    void shouldRejectGenerationWhenEscrituraNotFound() {
        when(escrituraRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testimonioService.generar(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
