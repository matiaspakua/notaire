package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDocumentoReingresado;
import com.licensis.notaire.dto.DtoGestionReingresoDocumentacion;
import com.licensis.notaire.dto.DtoReingresoDocumentacionRequest;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.DocumentoPresentado;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.PlantillaTramite;
import com.licensis.notaire.negocio.PlantillaTramitePK;
import com.licensis.notaire.negocio.TipoDeDocumento;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.DocumentoPresentadoRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.PlantillaTramiteRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.ReingresoDocumentacionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequirementCoverage({"CU43"})
@DisplayName("ReingresoDocumentacionService Tests")
@ExtendWith(MockitoExtension.class)
class ReingresoDocumentacionServiceTest {

    @Mock
    private GestionDeEscrituraRepository gestionRepository;

    @Mock
    private TramiteRepository tramiteRepository;

    @Mock
    private PlantillaTramiteRepository plantillaTramiteRepository;

    @Mock
    private DocumentoPresentadoRepository documentoPresentadoRepository;

    private ReingresoDocumentacionService service;

    private GestionDeEscritura gestion;
    private TipoDeTramite tipoTramite;
    private Tramite tramite;
    private TipoDeDocumento tipoDocumento;

    @BeforeEach
    void setUp() {
        service = new ReingresoDocumentacionService(gestionRepository, tramiteRepository,
                plantillaTramiteRepository, documentoPresentadoRepository);

        gestion = new GestionDeEscritura();
        gestion.setIdGestion(1);
        gestion.setNumero(100);
        gestion.setEncabezado("Compraventa");

        tipoTramite = new TipoDeTramite();
        tipoTramite.setIdTipoTramite(5);
        tipoTramite.setNombre("Compraventa");

        tramite = new Tramite();
        tramite.setIdTramite(10);
        tramite.setFkIdGestion(gestion);
        tramite.setFkIdTipoTramite(tipoTramite);

        tipoDocumento = new TipoDeDocumento();
        tipoDocumento.setIdTipoDocumento(7);
        tipoDocumento.setNombre("Certificado de Dominio");
        tipoDocumento.setVence(true);
        tipoDocumento.setDiasVencimiento(30);
        tipoDocumento.setQuienEntrega("Cliente");
    }

    @Nested
    @DisplayName("Obtener documentación necesaria de una gestión")
    class ObtenerDocumentacionNecesariaTests {

        @Test
        @DisplayName("Devuelve los trámites de la gestión con su documentación necesaria")
        void shouldReturnTramitesWithDocumentacionNecesaria() {
            PlantillaTramite plantilla = new PlantillaTramite(new PlantillaTramitePK(5, 7));
            plantilla.setTipoDeDocumento(tipoDocumento);
            plantilla.setTipoDeTramite(tipoTramite);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramite));
            when(plantillaTramiteRepository.findByTipoDeTramiteIdTipoTramite(5)).thenReturn(List.of(plantilla));

            DtoGestionReingresoDocumentacion resultado = service.obtenerDocumentacionNecesaria(1);

            assertThat(resultado.idGestion()).isEqualTo(1);
            assertThat(resultado.numero()).isEqualTo(100);
            assertThat(resultado.tramites()).hasSize(1);
            assertThat(resultado.tramites().get(0).idTramite()).isEqualTo(10);
            assertThat(resultado.tramites().get(0).tipoTramiteNombre()).isEqualTo("Compraventa");
            assertThat(resultado.tramites().get(0).documentosNecesarios()).hasSize(1);
            assertThat(resultado.tramites().get(0).documentosNecesarios().get(0).nombre())
                    .isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Devuelve documentación necesaria vacía cuando el trámite no tiene PlantillaTramite")
        void shouldReturnEmptyDocumentacionWhenNoPlantilla() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findByFkIdGestionIdGestion(1)).thenReturn(List.of(tramite));
            when(plantillaTramiteRepository.findByTipoDeTramiteIdTipoTramite(5)).thenReturn(List.of());

            DtoGestionReingresoDocumentacion resultado = service.obtenerDocumentacionNecesaria(1);

            assertThat(resultado.tramites()).hasSize(1);
            assertThat(resultado.tramites().get(0).documentosNecesarios()).isEmpty();
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenGestionNotFound() {
            when(gestionRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.obtenerDocumentacionNecesaria(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reingresar un tipo de documento")
    class ReingresarTests {

        @Test
        @DisplayName("Crea un DocumentoPresentado con reingresado=true heredando datos del TipoDeDocumento")
        void shouldCreateDocumentoPresentadoWhenValid() {
            PlantillaTramite plantilla = new PlantillaTramite(new PlantillaTramitePK(5, 7));
            plantilla.setTipoDeDocumento(tipoDocumento);
            plantilla.setTipoDeTramite(tipoTramite);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findById(10)).thenReturn(Optional.of(tramite));
            when(plantillaTramiteRepository.findById(new PlantillaTramitePK(5, 7))).thenReturn(Optional.of(plantilla));
            when(documentoPresentadoRepository.save(any(DocumentoPresentado.class))).thenAnswer(invocation -> {
                DocumentoPresentado doc = invocation.getArgument(0);
                doc.setIdDocumentoPresentado(50);
                return doc;
            });

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);
            DtoDocumentoReingresado resultado = service.reingresar(1, request);

            assertThat(resultado.idDocumentoPresentado()).isEqualTo(50);
            assertThat(resultado.idTramite()).isEqualTo(10);
            assertThat(resultado.idTipoDocumento()).isEqualTo(7);
            assertThat(resultado.nombre()).isEqualTo("Certificado de Dominio");
            assertThat(resultado.vence()).isTrue();
            assertThat(resultado.diasVencimiento()).isEqualTo(30);
            assertThat(resultado.quienEntrega()).isEqualTo("Cliente");
            assertThat(resultado.reingresado()).isTrue();

            ArgumentCaptor<DocumentoPresentado> captor = ArgumentCaptor.forClass(DocumentoPresentado.class);
            org.mockito.Mockito.verify(documentoPresentadoRepository).save(captor.capture());
            assertThat(captor.getValue().getReingresado()).isTrue();
            assertThat(captor.getValue().getFkIdTramite()).isSameAs(tramite);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el tipo de documento no forma parte de la PlantillaTramite")
        void shouldThrowWhenTipoDocumentoNotInPlantilla() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findById(10)).thenReturn(Optional.of(tramite));
            when(plantillaTramiteRepository.findById(new PlantillaTramitePK(5, 7))).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reingresar(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el trámite no pertenece a la gestión")
        void shouldThrowWhenTramiteDoesNotBelongToGestion() {
            GestionDeEscritura otraGestion = new GestionDeEscritura();
            otraGestion.setIdGestion(2);
            tramite.setFkIdGestion(otraGestion);

            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findById(10)).thenReturn(Optional.of(tramite));

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reingresar(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el trámite no existe")
        void shouldThrowWhenTramiteNotFound() {
            when(gestionRepository.findById(1)).thenReturn(Optional.of(gestion));
            when(tramiteRepository.findById(999)).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(999, 7);

            assertThatThrownBy(() -> service.reingresar(1, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenGestionNotFound() {
            when(gestionRepository.findById(999)).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reingresar(999, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
