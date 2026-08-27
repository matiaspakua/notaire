package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.service.EscrituraFirmaService;
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
@DisplayName("EscrituraFirmaService Tests")
@ExtendWith(MockitoExtension.class)
class EscrituraFirmaServiceTest {

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private FolioRepository folioRepository;

    @InjectMocks
    private EscrituraFirmaService escrituraFirmaService;

    private Escritura escritura;

    @BeforeEach
    void setUp() {
        escritura = new Escritura();
        escritura.setIdEscritura(1);
        escritura.setNumero(100);
        escritura.setEstado(ConstantesNegocio.ESCRITURA_SIN_FIRMAR);
    }

    @Test
    @DisplayName("Should sign escritura when unsigned and folio is assigned")
    void shouldSignEscrituraWhenUnsignedWithFolio() {
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));
        when(folioRepository.existsByFkIdEscrituraIdEscritura(1)).thenReturn(true);
        when(escrituraRepository.save(escritura)).thenReturn(escritura);

        Escritura firmada = escrituraFirmaService.firmar(1);

        assertThat(firmada.getEstado()).isEqualTo(ConstantesNegocio.ESCRITURA_FIRMADA);
        verify(escrituraRepository).save(escritura);
    }

    @Test
    @DisplayName("Should reject signing when escritura is already firmada")
    void shouldRejectSignWhenAlreadySigned() {
        escritura.setEstado(ConstantesNegocio.ESCRITURA_FIRMADA);
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));

        assertThatThrownBy(() -> escrituraFirmaService.firmar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Sin Firmar");
    }

    @Test
    @DisplayName("Should reject signing when escritura has no folio assigned")
    void shouldRejectSignWhenNoFolioAssigned() {
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));
        when(folioRepository.existsByFkIdEscrituraIdEscritura(1)).thenReturn(false);

        assertThatThrownBy(() -> escrituraFirmaService.firmar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("folio");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when escritura does not exist")
    void shouldRejectSignWhenEscrituraNotFound() {
        when(escrituraRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> escrituraFirmaService.firmar(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
