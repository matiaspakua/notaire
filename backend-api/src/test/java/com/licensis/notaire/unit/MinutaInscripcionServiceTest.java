package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.MinutaInscripcion;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.MinutaInscripcionRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.MinutaInscripcionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU82"})
@DisplayName("MinutaInscripcionService Tests")
@ExtendWith(MockitoExtension.class)
class MinutaInscripcionServiceTest {

    @Mock
    private MinutaInscripcionRepository minutaInscripcionRepository;

    @Mock
    private EscrituraRepository escrituraRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    private MinutaInscripcionService minutaInscripcionService;

    private Escritura escritura;
    private Inmueble inmueble;
    private Tramite tramite;

    @BeforeEach
    void setUp() {
        minutaInscripcionService = new MinutaInscripcionService(minutaInscripcionRepository, escrituraRepository,
                tramiteRepository);

        escritura = new Escritura();
        escritura.setIdEscritura(1);
        escritura.setEstado(ConstantesNegocio.ESCRITURA_FIRMADA);

        inmueble = new Inmueble();
        inmueble.setIdInmueble(1);
        inmueble.setNomenclaturaCatastral("123-456-789");
        inmueble.setValuacionFiscal(1000f);
        inmueble.setDomicilio("Calle Falsa 123");
        inmueble.setMatricula("M-1");
        inmueble.setTomoFolioFinca("T1-F2-FN3");
        inmueble.setLinderos("Norte, Sur, Este, Oeste");

        tramite = new Tramite();
        tramite.setIdTramite(10);
        tramite.setFkIdInmueble(inmueble);
        tramite.setFkIdEscritura(escritura);
    }

    @Test
    @DisplayName("Should generate minuta en estado Generada with complete data")
    void shouldGenerateMinutaWhenDataIsComplete() {
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));
        when(tramiteRepository.findByFkIdEscrituraIdEscritura(1)).thenReturn(List.of(tramite));
        when(minutaInscripcionRepository.findTopByOrderByNumeroDesc()).thenReturn(Optional.empty());
        when(minutaInscripcionRepository.save(any(MinutaInscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        MinutaInscripcion minuta = minutaInscripcionService.generar(1);

        assertThat(minuta.getNumero()).isEqualTo(1);
        assertThat(minuta.getEstado()).isEqualTo(ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA);
        assertThat(minuta.getFkIdEscritura()).isEqualTo(escritura);
        assertThat(minuta.getFechaGeneracion()).isNotNull();
    }

    @Test
    @DisplayName("Should reject generation when catastral/registral data is incomplete")
    void shouldRejectGenerationWhenDataIsIncomplete() {
        inmueble.setMatricula(null);
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));
        when(tramiteRepository.findByFkIdEscrituraIdEscritura(1)).thenReturn(List.of(tramite));

        assertThatThrownBy(() -> minutaInscripcionService.generar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("matrícula");
    }

    @Test
    @DisplayName("Should reject generation when escritura is not firmada")
    void shouldRejectGenerationWhenEscrituraNotFirmada() {
        escritura.setEstado(ConstantesNegocio.ESCRITURA_SIN_FIRMAR);
        when(escrituraRepository.findById(1)).thenReturn(Optional.of(escritura));

        assertThatThrownBy(() -> minutaInscripcionService.generar(1))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("firmada");
    }

    @Test
    @DisplayName("Should throw when escritura does not exist")
    void shouldThrowWhenEscrituraNotFound() {
        when(escrituraRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> minutaInscripcionService.generar(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should register presentacion and transition to Presentado")
    void shouldRegisterPresentacion() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));
        when(minutaInscripcionRepository.save(any(MinutaInscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        MinutaInscripcion result = minutaInscripcionService.presentar(1, new Date(), "ENT-123");

        assertThat(result.getEstado()).isEqualTo(ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA);
        assertThat(result.getNumeroEntradaRegistral()).isEqualTo("ENT-123");
        assertThat(result.getFechaPresentacion()).isNotNull();
    }

    @Test
    @DisplayName("Should reject presentacion when minuta is not en estado Generada")
    void shouldRejectPresentacionWhenNotGenerada() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));

        assertThatThrownBy(() -> minutaInscripcionService.presentar(1, new Date(), "ENT-123"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should register observacion and transition to Observado")
    void shouldRegisterObservacion() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));
        when(minutaInscripcionRepository.save(any(MinutaInscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        MinutaInscripcion result = minutaInscripcionService.observar(1, "Falta plano de mensura", new Date());

        assertThat(result.getEstado()).isEqualTo(ConstantesNegocio.MINUTA_INSCRIPCION_OBSERVADA);
        assertThat(result.getObservacionesRegistro()).isEqualTo("Falta plano de mensura");
        assertThat(result.getFechaSubsanacion()).isNotNull();
    }

    @Test
    @DisplayName("Should reject observacion when minuta is not presentada")
    void shouldRejectObservacionWhenNotPresentada() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_GENERADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));

        assertThatThrownBy(() -> minutaInscripcionService.observar(1, "Observación", new Date()))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should register inscripcion definitiva and transition to Inscripto")
    void shouldRegisterInscripcionDefinitiva() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_PRESENTADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));
        when(minutaInscripcionRepository.save(any(MinutaInscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        MinutaInscripcion result = minutaInscripcionService.inscribir(1, new Date(), "INS-456");

        assertThat(result.getEstado()).isEqualTo(ConstantesNegocio.MINUTA_INSCRIPCION_INSCRIPTA);
        assertThat(result.getNumeroInscripcionDefinitivo()).isEqualTo("INS-456");
        assertThat(result.getFechaRecepcion()).isNotNull();
    }

    @Test
    @DisplayName("Should reject inscripcion definitiva when minuta is not presentada")
    void shouldRejectInscripcionWhenNotPresentada() {
        MinutaInscripcion minuta = buildMinuta(1, ConstantesNegocio.MINUTA_INSCRIPCION_OBSERVADA);
        when(minutaInscripcionRepository.findById(1)).thenReturn(Optional.of(minuta));

        assertThatThrownBy(() -> minutaInscripcionService.inscribir(1, new Date(), "INS-456"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("Should throw when minuta does not exist")
    void shouldThrowWhenMinutaNotFound() {
        when(minutaInscripcionRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> minutaInscripcionService.presentar(999, new Date(), "ENT-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private MinutaInscripcion buildMinuta(Integer id, String estado) {
        MinutaInscripcion minuta = new MinutaInscripcion();
        minuta.setIdMinutaInscripcion(id);
        minuta.setNumero(1);
        minuta.setEstado(estado);
        minuta.setFkIdEscritura(escritura);
        return minuta;
    }
}
