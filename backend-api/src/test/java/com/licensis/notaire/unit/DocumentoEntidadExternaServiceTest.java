package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDocumentoEntidadExterna;
import com.licensis.notaire.dto.DtoGestionDocumentosEntidadesExternas;
import com.licensis.notaire.dto.DtoMovimientoDocumentoEntidadExterna;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.ConstantesNegocio;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.DocumentoEntidadExternaService;
import com.licensis.notaire.service.GestionTransitionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU10"})
@DisplayName("DocumentoEntidadExternaService Tests")
@ExtendWith(MockitoExtension.class)
class DocumentoEntidadExternaServiceTest {

    @Mock
    private GestionDeEscrituraRepository gestionRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private DocumentoPresentadoRepository documentoPresentadoRepository;

    @Mock
    private GestionTransitionService gestionTransitionService;

    @InjectMocks
    private DocumentoEntidadExternaService documentoEntidadExternaService;

    private GestionDeEscritura gestion;
    private Tramite tramite;
    private DocumentoPresentado documento;

    @BeforeEach
    void setUp() {
        Persona escribano = new Persona();
        escribano.setNombre("Ana");
        escribano.setApellido("Notaria");

        gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        gestion.setNumero(100);
        gestion.setEncabezado("Compraventa");
        gestion.setFechaInicio(new Date());
        gestion.setFkIdPersonaEscribano(escribano);

        tramite = new Tramite();
        tramite.setIdTramite(10);
        tramite.setFkIdGestion(gestion);

        documento = new DocumentoPresentado();
        documento.setIdDocumentoPresentado(50);
        documento.setNombre("Certificado de Dominio");
        documento.setQuienEntrega(ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA);
        documento.setFkIdTramite(tramite);
        documento.setEntregado(false);
    }

    @Nested
    @DisplayName("Obtener documentos de entidades externas de una gestión")
    class ObtenerDocumentosTests {

        @Test
        @DisplayName("Devuelve la gestión con sus documentos de entidad externa y nomenclatura catastral")
        void shouldReturnGestionWithDocumentosAndNomenclatura() {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("12-34-56");
            tramite.setFkIdInmueble(inmueble);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramite));
            when(documentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(
                    1, ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(documento));

            DtoGestionDocumentosEntidadesExternas resultado = documentoEntidadExternaService.obtenerDocumentos(1);

            assertThat(resultado.idGestion()).isEqualTo(1);
            assertThat(resultado.numero()).isEqualTo(100);
            assertThat(resultado.escribano()).isEqualTo("Ana Notaria");
            assertThat(resultado.nomenclaturaCatastral()).isEqualTo("12-34-56");
            assertThat(resultado.documentos()).hasSize(1);
            assertThat(resultado.documentos().get(0).nombre()).isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Nomenclatura catastral es null cuando ningún trámite tiene inmueble asociado")
        void shouldReturnNullNomenclaturaWhenNoInmueble() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramite));
            when(documentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(
                    1, ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of());

            DtoGestionDocumentosEntidadesExternas resultado = documentoEntidadExternaService.obtenerDocumentos(1);

            assertThat(resultado.nomenclaturaCatastral()).isNull();
            assertThat(resultado.documentos()).isEmpty();
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenGestionNotFound() {
            when(gestionRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentoEntidadExternaService.obtenerDocumentos(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registrar movimiento de un documento de entidad externa")
    class RegistrarMovimientoTests {

        @Test
        @DisplayName("Actualiza los campos de movimiento del documento")
        void shouldUpdateMovementFields() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(documentoPresentadoRepository.findById(50)).thenReturn(Optional.of(documento));
            when(documentoPresentadoRepository.save(any(DocumentoPresentado.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Date fechaIngreso = new Date();
            DtoMovimientoDocumentoEntidadExterna movimiento = new DtoMovimientoDocumentoEntidadExterna(
                    true, 7, fechaIngreso, null, false, 1500f, null, null, "Retirado a tiempo", false);

            DtoDocumentoEntidadExterna resultado =
                    documentoEntidadExternaService.registrarMovimiento(1, 50, movimiento);

            assertThat(resultado.preparado()).isTrue();
            assertThat(resultado.numeroCarton()).isEqualTo(7);
            assertThat(resultado.fechaIngreso()).isEqualTo(fechaIngreso);
            assertThat(resultado.importeAPagar()).isEqualTo(1500f);
            assertThat(resultado.observaciones()).isEqualTo("Retirado a tiempo");
            assertThat(resultado.entregado()).isFalse();
            verify(gestionTransitionService, never()).transicionar(anyInt(), any());
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el documento no pertenece a la gestión indicada")
        void shouldThrowWhenDocumentDoesNotBelongToGestion() {
            GestionDeEscritura otraGestion = new GestionDeEscritura();
            otraGestion.setIdGestion(2);
            Tramite otroTramite = new Tramite();
            otroTramite.setIdTramite(20);
            otroTramite.setFkIdGestion(otraGestion);
            documento.setFkIdTramite(otroTramite);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(documentoPresentadoRepository.findById(50)).thenReturn(Optional.of(documento));

            DtoMovimientoDocumentoEntidadExterna movimiento = new DtoMovimientoDocumentoEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentoEntidadExternaService.registrarMovimiento(1, 50, movimiento))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el documento no es de entidad externa")
        void shouldThrowWhenDocumentIsNotEntidadExterna() {
            documento.setQuienEntrega(ConstantesNegocio.DOCUMENTACION_CLIENTE);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(documentoPresentadoRepository.findById(50)).thenReturn(Optional.of(documento));

            DtoMovimientoDocumentoEntidadExterna movimiento = new DtoMovimientoDocumentoEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentoEntidadExternaService.registrarMovimiento(1, 50, movimiento))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el documento no existe")
        void shouldThrowWhenDocumentNotFound() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(documentoPresentadoRepository.findById(999)).thenReturn(Optional.empty());

            DtoMovimientoDocumentoEntidadExterna movimiento = new DtoMovimientoDocumentoEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentoEntidadExternaService.registrarMovimiento(1, 999, movimiento))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Intentar completar la documentación de entidades externas")
    class IntentarCompletarDocumentacionTests {

        @Test
        @DisplayName("Transiciona la gestión a Documentacion Completa cuando todos los documentos quedan entregados")
        void shouldTransitionGestionWhenAllDocumentsDelivered() {
            documento.setEntregado(true);
            when(documentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(
                    1, ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(documento));

            documentoEntidadExternaService.intentarCompletarDocumentacion(1);

            ArgumentCaptor<Integer> idGestionCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<String> estadoCaptor = ArgumentCaptor.forClass(String.class);
            verify(gestionTransitionService, times(1)).transicionar(idGestionCaptor.capture(), estadoCaptor.capture());
            assertThat(idGestionCaptor.getValue()).isEqualTo(1);
            assertThat(estadoCaptor.getValue()).isEqualTo(ConstantesNegocio.GESTION_CON_DOCUMENTACION_COMPLETA);
        }

        @Test
        @DisplayName("No transiciona la gestión cuando quedan documentos sin entregar")
        void shouldNotTransitionWhenDocumentsPending() {
            when(documentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(
                    1, ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(documento));

            documentoEntidadExternaService.intentarCompletarDocumentacion(1);

            verify(gestionTransitionService, never()).transicionar(anyInt(), any());
        }

        @Test
        @DisplayName("No propaga la excepción si el workflow no admite la transición automática")
        void shouldSwallowBusinessValidationExceptionOnAutoTransition() {
            documento.setEntregado(true);
            when(documentoPresentadoRepository.findByFkIdTramiteFkIdGestionIdGestionAndQuienEntrega(
                    1, ConstantesNegocio.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(documento));
            when(gestionTransitionService.transicionar(eq(1), eq(ConstantesNegocio.GESTION_CON_DOCUMENTACION_COMPLETA)))
                    .thenThrow(new BusinessValidationException("Transición no permitida"));

            documentoEntidadExternaService.intentarCompletarDocumentacion(1);

            verify(gestionTransitionService, times(1)).transicionar(1, ConstantesNegocio.GESTION_CON_DOCUMENTACION_COMPLETA);
        }
    }
}
