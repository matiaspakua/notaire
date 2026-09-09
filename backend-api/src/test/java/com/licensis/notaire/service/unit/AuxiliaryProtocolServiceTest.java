package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.AuxiliaryProtocolService;
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
class AuxiliaryProtocolServiceTest {

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private DeedRepository deedRepository;

    @InjectMocks
    private AuxiliaryProtocolService protocoloAuxiliaryService;

    private Folio folioAuxiliaryDisponible() {
        FolioType typeAuxiliary = new FolioType("Protocolo Auxiliar");
        typeAuxiliary.setIsAuxiliary(true);
        Folio folio = new Folio();
        folio.setIdFolio(1);
        folio.setFkIdFolioType(typeAuxiliary);
        return folio;
    }

    @Test
    @DisplayName("Should keep auxiliar numbering independent from the numeración of Protocolo Principal")
    void shouldKeepAuxiliaryNumberingIndependentFromPrincipal() {
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.of(5));

        int siguiente = protocoloAuxiliaryService.calcularSiguienteNumberAuxiliary();

        assertThat(siguiente).isEqualTo(6);
    }

    @Test
    @DisplayName("Should start auxiliar numbering at one when no auxiliar deed exists yet")
    void shouldStartAuxiliaryNumberingAtOne() {
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.empty());

        int siguiente = protocoloAuxiliaryService.calcularSiguienteNumberAuxiliary();

        assertThat(siguiente).isEqualTo(1);
    }

    @Test
    @DisplayName("Should not generate carpeta de trámite for a Protocolo Auxiliar deed")
    void shouldNotGenerateFolderForAuxiliaryDeed() {
        Folio folio = folioAuxiliaryDisponible();
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.empty());
        when(deedRepository.save(any(Deed.class))).thenAnswer(inv -> inv.getArgument(0));

        Deed result = protocoloAuxiliaryService.iniciarDeed(1, "body del acta", new Date());

        assertThat(result.getProcedureList()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should reject starting an deed on a folio that is not auxiliar")
    void shouldRejectDeedOnNonAuxiliaryFolio() {
        FolioType principal = new FolioType("Protocolo Principal");
        Folio folio = new Folio();
        folio.setIdFolio(2);
        folio.setFkIdFolioType(principal);
        when(folioRepository.findById(2)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> protocoloAuxiliaryService.iniciarDeed(2, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an deed on an auxiliar folio already linked to another deed")
    void shouldRejectDeedOnAlreadyLinkedFolio() {
        Folio folio = folioAuxiliaryDisponible();
        folio.setFkIdDeed(new Deed(99));
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> protocoloAuxiliaryService.iniciarDeed(1, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an deed when no idFolio is provided")
    void shouldRejectDeedWhenNoFolioIndicated() {
        assertThatThrownBy(() -> protocoloAuxiliaryService.iniciarDeed(null, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting an deed when the indicated folio does not exist")
    void shouldRejectDeedWhenFolioNotFound() {
        when(folioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> protocoloAuxiliaryService.iniciarDeed(99, "body", new Date()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
