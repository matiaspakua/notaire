package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.application.usecase.deed.DeedSigningService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU06"})
@DisplayName("DeedSigningService Tests")
@ExtendWith(MockitoExtension.class)
class DeedSigningServiceTest {

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private FolioRepository folioRepository;

    @InjectMocks
    private DeedSigningService deedSigningService;

    private Deed deed;

    @BeforeEach
    void setUp() {
        deed = new Deed();
        deed.setIdDeed(1);
        deed.setNumber(100);
        deed.setStatus(BusinessConstants.DeedUnsigned);
    }

    @Test
    @DisplayName("Should sign deed when unsigned and folio is assigned")
    void shouldSignDeedWhenUnsignedWithFolio() {
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));
        when(folioRepository.existsByFkIdDeedIdDeed(1)).thenReturn(true);
        when(deedRepository.save(deed)).thenReturn(deed);

        Deed signed = deedSigningService.sign(1);

        assertThat(signed.getStatus()).isEqualTo(BusinessConstants.DeedSigned);
        verify(deedRepository).save(deed);
    }

    @Test
    @DisplayName("Should reject signing when the deed is already signed")
    void shouldRejectSignWhenAlreadySigned() {
        deed.setStatus(BusinessConstants.DeedSigned);
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));

        assertThatThrownBy(() -> deedSigningService.sign(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Sin Firmar");
    }

    @Test
    @DisplayName("Should reject signing when deed has no folio assigned")
    void shouldRejectSignWhenNoFolioAssigned() {
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));
        when(folioRepository.existsByFkIdDeedIdDeed(1)).thenReturn(false);

        assertThatThrownBy(() -> deedSigningService.sign(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("folio");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deed does not exist")
    void shouldRejectSignWhenDeedNotFound() {
        when(deedRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deedSigningService.sign(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
