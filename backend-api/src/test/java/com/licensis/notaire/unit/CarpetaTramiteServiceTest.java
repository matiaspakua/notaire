package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.CarpetaTramiteRepository;
import com.licensis.notaire.service.CarpetaTramiteService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU85"})
@DisplayName("CarpetaTramiteService Tests")
@ExtendWith(MockitoExtension.class)
class CarpetaTramiteServiceTest {

    @Mock
    private CarpetaTramiteRepository carpetaTramiteRepository;

    @InjectMocks
    private CarpetaTramiteService carpetaTramiteService;

    private GestionDeEscritura gestion;
    private Tramite tramite;

    @BeforeEach
    void setUp() {
        gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        tramite = new Tramite();
        tramite.setIdTramite(10);
        tramite.setFkIdGestion(gestion);
    }

    @Test
    @DisplayName("Should generate carpeta activa numerada al alta de un trámite")
    void shouldGenerateActiveCarpetaForNewTramite() {
        when(carpetaTramiteRepository.findTopByOrderByNumeroDesc()).thenReturn(Optional.empty());
        when(carpetaTramiteRepository.save(any(CarpetaTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        CarpetaTramite carpeta = carpetaTramiteService.generarCarpetaParaTramite(tramite);

        assertThat(carpeta.getNumero()).isEqualTo(1);
        assertThat(carpeta.getEstado()).isEqualTo("Activa");
        assertThat(carpeta.getFkIdGestion()).isEqualTo(gestion);
        assertThat(carpeta.getFkIdTramite()).isEqualTo(tramite);
    }

    @Test
    @DisplayName("Should increment numero based on the last generated carpeta")
    void shouldIncrementNumeroFromLastCarpeta() {
        CarpetaTramite ultima = new CarpetaTramite();
        ultima.setNumero(7);
        when(carpetaTramiteRepository.findTopByOrderByNumeroDesc()).thenReturn(Optional.of(ultima));
        when(carpetaTramiteRepository.save(any(CarpetaTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        CarpetaTramite carpeta = carpetaTramiteService.generarCarpetaParaTramite(tramite);

        assertThat(carpeta.getNumero()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should put carpeta en espera when motivo is provided")
    void shouldPutCarpetaEnEsperaWithMotivo() {
        CarpetaTramite carpeta = new CarpetaTramite();
        carpeta.setIdCarpeta(1);
        carpeta.setEstado("Activa");
        when(carpetaTramiteRepository.findById(1)).thenReturn(Optional.of(carpeta));
        when(carpetaTramiteRepository.save(any(CarpetaTramite.class))).thenAnswer(inv -> inv.getArgument(0));

        CarpetaTramite result = carpetaTramiteService.ponerEnEspera(1, "Falta documentación del titular");

        assertThat(result.getEstado()).isEqualTo("Espera");
        assertThat(result.getMotivoEspera()).isEqualTo("Falta documentación del titular");
        ArgumentCaptor<CarpetaTramite> captor = ArgumentCaptor.forClass(CarpetaTramite.class);
        verify(carpetaTramiteRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("Espera");
    }

    @Test
    @DisplayName("Should reject poner en espera without a motivo (CU85 - Excepción 3.1)")
    void shouldRejectEsperaWithoutMotivo() {
        assertThatThrownBy(() -> carpetaTramiteService.ponerEnEspera(1, "  "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("motivo");
    }

    @Test
    @DisplayName("Should throw when carpeta does not exist")
    void shouldThrowWhenCarpetaNotFound() {
        when(carpetaTramiteRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carpetaTramiteService.ponerEnEspera(999, "Motivo válido"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should find carpetas by gestión")
    void shouldFindCarpetasByGestion() {
        CarpetaTramite carpeta = new CarpetaTramite();
        carpeta.setIdCarpeta(1);
        when(carpetaTramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(carpeta));

        List<CarpetaTramite> carpetas = carpetaTramiteService.findByGestion(1);

        assertThat(carpetas).containsExactly(carpeta);
    }

    @Test
    @DisplayName("Should find carpeta by trámite")
    void shouldFindCarpetaByTramite() {
        CarpetaTramite carpeta = new CarpetaTramite();
        carpeta.setIdCarpeta(1);
        when(carpetaTramiteRepository.findByFkIdTramiteIdTramite(10)).thenReturn(Optional.of(carpeta));

        Optional<CarpetaTramite> result = carpetaTramiteService.findByTramite(10);

        assertThat(result).contains(carpeta);
    }
}
