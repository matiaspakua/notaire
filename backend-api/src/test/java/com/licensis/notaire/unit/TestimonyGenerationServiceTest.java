package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.service.TestimonyGenerationVerificationService;
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
class TestimonyGenerationServiceTest {

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private TestimonyRepository testimonyRepository;

    @InjectMocks
    private TestimonyGenerationVerificationService testimonyService;

    private Deed deed;

    @BeforeEach
    void setUp() {
        deed = new Deed();
        deed.setIdDeed(1);
        deed.setNumber(100);
        deed.setStatus(BusinessConstants.DeedFIRMADA);
    }

    @Test
    @DisplayName("Should generate testimony from a signed deed")
    void shouldGenerateTestimonyFromSignedDeed() {
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));
        when(testimonyRepository.save(org.mockito.ArgumentMatchers.any(Testimony.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Testimony generado = testimonyService.generate(1);

        ArgumentCaptor<Testimony> captor = ArgumentCaptor.forClass(Testimony.class);
        verify(testimonyRepository).save(captor.capture());

        assertThat(generado).isNotNull();
        assertThat(generado.getFkIdDeed().getIdDeed()).isEqualTo(1);
        assertThat(generado.getVerified()).isFalse();
        assertThat(captor.getValue().getFkIdDeed().getIdDeed()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject generation when deed is not signed")
    void shouldRejectGenerationWhenDeedNotSigned() {
        deed.setStatus(BusinessConstants.DeedSINFIRMAR);
        when(deedRepository.findById(1)).thenReturn(Optional.of(deed));

        assertThatThrownBy(() -> testimonyService.generate(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Firmada");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deed does not exist")
    void shouldRejectGenerationWhenDeedNotFound() {
        when(deedRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testimonyService.generate(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
