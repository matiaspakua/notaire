package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.negocio.PlantillaCostoDocumento;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.repository.PlantillaCostoDocumentoRepository;
import com.licensis.notaire.repository.TipoDeDocumentoRepository;
import com.licensis.notaire.repository.TipoDeTramiteRepository;
import com.licensis.notaire.service.PlantillaCostoDocumentoService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlantillaCostoDocumentoService Unit Tests (Issue #823)")
class PlantillaCostoDocumentoServiceTest {

    @Mock
    private PlantillaCostoDocumentoRepository plantillaCostoDocumentoRepository;

    @Mock
    private TipoDeTramiteRepository tipoDeTramiteRepository;

    @Mock
    private TipoDeDocumentoRepository tipoDeDocumentoRepository;

    @InjectMocks
    private PlantillaCostoDocumentoService plantillaCostoDocumentoService;

    private TipoDeTramite tipoDeTramite;
    private TipoDeDocumento tipoDeDocumento;

    @BeforeEach
    void setUp() {
        tipoDeTramite = new TipoDeTramite();
        tipoDeTramite.setIdTipoTramite(1);

        tipoDeDocumento = new TipoDeDocumento();
        tipoDeDocumento.setIdTipoDocumento(1);
    }

    @Test
    @DisplayName("Should accept fixed cost for tipo de documento")
    void shouldAcceptFixedCostForTipoDocumento() {
        when(tipoDeTramiteRepository.findById(1)).thenReturn(Optional.of(tipoDeTramite));
        when(tipoDeDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDeDocumento));
        when(plantillaCostoDocumentoRepository.save(any(PlantillaCostoDocumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlantillaCostoDocumento result = plantillaCostoDocumentoService.crear(1, 1, 1500f, null);

        assertThat(result.getMontoFijo()).isEqualTo(1500f);
        assertThat(result.getPorcentajeVariable()).isNull();
    }

    @Test
    @DisplayName("Should accept variable cost for tipo de documento")
    void shouldAcceptVariableCostForTipoDocumento() {
        when(tipoDeTramiteRepository.findById(1)).thenReturn(Optional.of(tipoDeTramite));
        when(tipoDeDocumentoRepository.findById(1)).thenReturn(Optional.of(tipoDeDocumento));
        when(plantillaCostoDocumentoRepository.save(any(PlantillaCostoDocumento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PlantillaCostoDocumento result = plantillaCostoDocumentoService.crear(1, 1, null, 5f);

        assertThat(result.getPorcentajeVariable()).isEqualTo(5f);
        assertThat(result.getMontoFijo()).isNull();
    }

    @Test
    @DisplayName("Should reject when both fixed and variable cost provided")
    void shouldRejectWhenBothFixedAndVariableCostProvided() {
        assertThatThrownBy(() -> plantillaCostoDocumentoService.crear(1, 1, 1500f, 5f))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("exactamente uno");
    }

    @Test
    @DisplayName("Should reject when neither fixed nor variable cost provided")
    void shouldRejectWhenNeitherFixedNorVariableCostProvided() {
        assertThatThrownBy(() -> plantillaCostoDocumentoService.crear(1, 1, null, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("exactamente uno");
    }
}
