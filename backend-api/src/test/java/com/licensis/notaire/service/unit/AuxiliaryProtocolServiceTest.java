package com.licensis.notaire.service.unit;

import com.licensis.notaire.application.port.out.folio.FolioRepositoryPort;
import com.licensis.notaire.application.port.out.notebook.DeedOperationPort;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.application.usecase.notebook.AuxiliaryProtocolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuxiliaryProtocolService Unit Tests")
class AuxiliaryProtocolServiceTest {

    @Mock
    private FolioRepositoryPort folioRepository;

    @Mock
    private DeedOperationPort deedRepository;

    private AuxiliaryProtocolService auxiliaryProtocolService;

    @BeforeEach
    void setUp() {
        auxiliaryProtocolService = new AuxiliaryProtocolService(folioRepository, deedRepository);
    }

    private Folio availableAuxiliaryFolio() {
        FolioType typeAuxiliary = new FolioType("Protocolo Auxiliar");
        typeAuxiliary.setIsAuxiliary(true);
        Folio folio = new Folio();
        folio.setIdFolio(1);
        folio.setFkIdFolioType(typeAuxiliary);
        return folio;
    }

    @Test
    @DisplayName("Should keep auxiliary numbering independent of the main protocol numbering")
    void shouldKeepAuxiliaryNumberingIndependentFromPrincipal() {
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.of(5));

        int nextNumber = auxiliaryProtocolService.calculateNextNumberAuxiliary();

        assertThat(nextNumber).isEqualTo(6);
    }

    @Test
    @DisplayName("Should start auxiliary numbering at one when no auxiliary deed exists yet")
    void shouldStartAuxiliaryNumberingAtOne() {
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.empty());

        int nextNumber = auxiliaryProtocolService.calculateNextNumberAuxiliary();

        assertThat(nextNumber).isEqualTo(1);
    }

    @Test
    @DisplayName("Should not generate a procedure folder for an auxiliary-protocol deed")
    void shouldNotGenerateFolderForAuxiliaryDeed() {
        Folio folio = availableAuxiliaryFolio();
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));
        when(folioRepository.findMaxNumberDeedAuxiliary()).thenReturn(Optional.empty());
        when(deedRepository.save(any(Deed.class))).thenAnswer(inv -> inv.getArgument(0));

        Deed result = auxiliaryProtocolService.startDeed(1, "deed body", new Date());

        assertThat(result.getProcedureList()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should reject starting a deed on a folio that is not auxiliary")
    void shouldRejectDeedOnNonAuxiliaryFolio() {
        FolioType principal = new FolioType("Protocolo Principal");
        Folio folio = new Folio();
        folio.setIdFolio(2);
        folio.setFkIdFolioType(principal);
        when(folioRepository.findById(2)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> auxiliaryProtocolService.startDeed(2, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting a deed on an auxiliary folio already linked to another deed")
    void shouldRejectDeedOnAlreadyLinkedFolio() {
        Folio folio = availableAuxiliaryFolio();
        folio.setFkIdDeed(new Deed(99));
        when(folioRepository.findById(1)).thenReturn(Optional.of(folio));

        assertThatThrownBy(() -> auxiliaryProtocolService.startDeed(1, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should reject starting a deed when no idFolio is provided")
    void shouldRejectDeedWhenNoFolioIndicated() {
        assertThatThrownBy(() -> auxiliaryProtocolService.startDeed(null, "body", new Date()))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("The auxiliary folio where the deed starts is required");
    }

    @Test
    @DisplayName("Should reject starting a deed when the indicated folio does not exist")
    void shouldRejectDeedWhenFolioNotFound() {
        when(folioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auxiliaryProtocolService.startDeed(99, "body", new Date()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
