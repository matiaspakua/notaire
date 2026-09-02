package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.TipoDeFolio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.ProtocoloAuxiliarService;
import org.junit.jupiter.api.DisplayName;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ProtocoloAuxiliarService Unit Tests")
class ProtocoloAuxiliarServiceTest {

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private EscrituraRepository escrituraRepository;

    @InjectMocks
    private ProtocoloAuxiliarService protocoloAuxiliarService;

    private Folio folioAuxiliarDisponible() {
        TipoDeFolio tipoAuxiliar = new TipoDeFolio("Protocolo Auxiliar");
        tipoAuxiliar.setEsAuxiliar(true);
        Folio folio = new Folio();
        folio.setIdFolio(1);
        folio.setFkIdTipoFolio(tipoAuxiliar);
        return folio;
    }

    @Test
    @DisplayName("Should keep auxiliar numbering independent from the numeración of Protocolo Principal")
    void shouldKeepAuxiliarNumberingIndependentFromPrincipal() {
        when(folioRepository.findMaxNumeroEscrituraAuxiliar()).thenReturn(Optional.of(5));

        int siguiente = protocoloAuxiliarService.calcularSiguienteNumeroAuxiliar();

        assertThat(siguiente).isEqualTo(6);
    }

    @Test
    @DisplayName("Should start auxiliar numbering at one when no auxiliar escritura exists yet")
    void shouldStartAuxiliarNumberingAtOne() {
        when(folioRepository.findMaxNumeroEscrituraAuxiliar()).thenReturn(Optional.empty());

        int siguiente = protocoloAuxiliarService.calcularSiguienteNumeroAuxiliar();

        assertThat(siguiente).isEqualTo(1);
    }

    @Test
    @DisplayName("Should not generate carpeta de trámite for a Protocolo Auxiliar escritura")
    void shouldNotGenerateCarpetaForAuxiliarEscritura() {
        Folio folio = folioAuxiliarDisponible();
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));
        when(folioRepository.findMaxNumeroEscrituraAuxiliar()).thenReturn(Optional.empty());
        when(escrituraRepository.save(any(Escritura.class))).thenAnswer(inv -> inv.getArgument(0));

        Escritura result = protocoloAuxiliarService.iniciarEscritura(1, "cuerpo del acta", new Date());

        assertThat(result.getTramiteList()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should reject starting an escritura on a folio that is not auxiliar")
    void shouldRejectEscrituraOnNonAuxiliarFolio() {
        TipoDeFolio principal = new TipoDeFolio("Protocolo Principal");
        Folio folio = new Folio();
        folio.setIdFolio(2);
        folio.setFkIdTipoFolio(principal);
        when(folioRepository.findById(2)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> protocoloAuxiliarService.iniciarEscritura(2, "cuerpo", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an escritura on an auxiliar folio already linked to another escritura")
    void shouldRejectEscrituraOnAlreadyLinkedFolio() {
        Folio folio = folioAuxiliarDisponible();
        folio.setFkIdEscritura(new Escritura(99));
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> protocoloAuxiliarService.iniciarEscritura(1, "cuerpo", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an escritura when no idFolio is provided")
    void shouldRejectEscrituraWhenNoFolioIndicated() {
        assertThatThrownBy(() -> protocoloAuxiliarService.iniciarEscritura(null, "cuerpo", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an escritura when the indicated folio does not exist")
    void shouldRejectEscrituraWhenFolioNotFound() {
        when(folioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocoloAuxiliarService.iniciarEscritura(99, "cuerpo", new Date()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
