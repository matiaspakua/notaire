package com.licensis.notaire.unit;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.TestimonyRepository;
import com.licensis.notaire.service.TestimonyGenerationVerificationService;
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
class TestimonyVerificacionServiceTest {

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private TestimonyRepository testimonyRepository;

    @InjectMocks
    private TestimonyGenerationVerificationService testimonyService;

    private Testimony testimony;

    @BeforeEach
    void setUp() {
        testimony = new Testimony();
        testimony.setIdTestimony(5);
        testimony.setNumber(50);
    }

    @Test
    @DisplayName("Should verify testimony without observations")
    void shouldVerifyWithoutObservations() {
        when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
        when(testimonyRepository.save(testimony)).thenReturn(testimony);

        Testimony verified = testimonyService.verify(5, false, null);

        assertThat(verified.getVerified()).isTrue();
        assertThat(verified.getFlagged()).isFalse();
        assertThat(verified.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should verify testimony with observations")
    void shouldVerifyWithObservations() {
        when(testimonyRepository.findById(5)).thenReturn(Optional.of(testimony));
        when(testimonyRepository.save(testimony)).thenReturn(testimony);

        Testimony verified = testimonyService.verify(5, true, "Falta una firma");

        assertThat(verified.getVerified()).isTrue();
        assertThat(verified.getFlagged()).isTrue();
        assertThat(verified.getNotes()).isEqualTo("Falta una firma");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when testimony does not exist")
    void shouldRejectVerificationWhenTestimonyNotFound() {
        when(testimonyRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testimonyService.verify(999, false, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
