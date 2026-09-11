package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDocumentEntidadExterna;
import com.licensis.notaire.dto.DtoManagementDocumentsEntidadesExternas;
import com.licensis.notaire.dto.DtoMovementDocumentEntidadExterna;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.service.DocumentEntidadExternaService;
import com.licensis.notaire.service.ManagementTransitionService;
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
class DocumentEntidadExternaServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private SubmittedDocumentRepository submittedDocumentRepository;

    @Mock
    private ManagementTransitionService managementTransitionService;

    @InjectMocks
    private DocumentEntidadExternaService documentEntidadExternaService;

    private DeedManagement management;
    private Procedure procedure;
    private SubmittedDocument document;

    @BeforeEach
    void setUp() {
        Person notary = new Person();
        notary.setFirstName("Ana");
        notary.setLastName("Notaria");

        management = new DeedManagement();
        management.setIdManagement(1);
        management.setNumber(100);
        management.setEncabezado("Compraventa");
        management.setDateStart(new Date());
        management.setFkIdNotaryPerson(notary);

        procedure = new Procedure();
        procedure.setIdProcedure(10);
        procedure.setFkIdManagement(management);

        document = new SubmittedDocument();
        document.setIdSubmittedDocument(50);
        document.setName("Certificado de Dominio");
        document.setDeliveredBy(BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA);
        document.setFkIdProcedure(procedure);
        document.setDelivered(false);
    }

    @Nested
    @DisplayName("Obtener documents de entidades externas de una gestión")
    class ObtenerDocumentsTests {

        @Test
        @DisplayName("Devuelve la gestión con sus documents de entidad externa y nomenclatura catastral")
        void shouldReturnManagementWithDocumentsAndDesignation() {
            Property property = new Property();
            property.setCadastralDesignation("12-34-56");
            procedure.setFkIdProperty(property);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            DtoManagementDocumentsEntidadesExternas resultado = documentEntidadExternaService.obtenerDocuments(1);

            assertThat(resultado.idManagement()).isEqualTo(1);
            assertThat(resultado.number()).isEqualTo(100);
            assertThat(resultado.notary()).isEqualTo("Ana Notaria");
            assertThat(resultado.cadastralDesignation()).isEqualTo("12-34-56");
            assertThat(resultado.documents()).hasSize(1);
            assertThat(resultado.documents().get(0).name()).isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Nomenclatura catastral es null cuando ningún trámite tiene property asociado")
        void shouldReturnNullDesignationWhenNoProperty() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of());

            DtoManagementDocumentsEntidadesExternas resultado = documentEntidadExternaService.obtenerDocuments(1);

            assertThat(resultado.cadastralDesignation()).isNull();
            assertThat(resultado.documents()).isEmpty();
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> documentEntidadExternaService.obtenerDocuments(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registrar movimiento de un documento de entidad externa")
    class RegistrarMovementTests {

        @Test
        @DisplayName("Actualiza los campos de movimiento del documento")
        void shouldUpdateMovementFields() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(submittedDocumentRepository.findById(50)).thenReturn(Optional.of(document));
            when(submittedDocumentRepository.save(any(SubmittedDocument.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Date dateEntry = new Date();
            DtoMovementDocumentEntidadExterna movement = new DtoMovementDocumentEntidadExterna(
                    true, 7, dateEntry, null, false, 1500f, null, null, "Retirado a tiempo", false);

            DtoDocumentEntidadExterna resultado =
                    documentEntidadExternaService.registrarMovement(1, 50, movement);

            assertThat(resultado.prepared()).isTrue();
            assertThat(resultado.cardNumber()).isEqualTo(7);
            assertThat(resultado.dateEntry()).isEqualTo(dateEntry);
            assertThat(resultado.amountToPay()).isEqualTo(1500f);
            assertThat(resultado.notes()).isEqualTo("Retirado a tiempo");
            assertThat(resultado.delivered()).isFalse();
            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el documento no pertenece a la gestión indicada")
        void shouldThrowWhenDocumentDoesNotBelongToManagement() {
            DeedManagement otraManagement = new DeedManagement();
            otraManagement.setIdManagement(2);
            Procedure otroProcedure = new Procedure();
            otroProcedure.setIdProcedure(20);
            otroProcedure.setFkIdManagement(otraManagement);
            document.setFkIdProcedure(otroProcedure);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(submittedDocumentRepository.findById(50)).thenReturn(Optional.of(document));

            DtoMovementDocumentEntidadExterna movement = new DtoMovementDocumentEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentEntidadExternaService.registrarMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el documento no es de entidad externa")
        void shouldThrowWhenDocumentIsNotEntidadExterna() {
            document.setDeliveredBy(BusinessConstants.DOCUMENTACIONClient);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(submittedDocumentRepository.findById(50)).thenReturn(Optional.of(document));

            DtoMovementDocumentEntidadExterna movement = new DtoMovementDocumentEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentEntidadExternaService.registrarMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el documento no existe")
        void shouldThrowWhenDocumentNotFound() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(submittedDocumentRepository.findById(999)).thenReturn(Optional.empty());

            DtoMovementDocumentEntidadExterna movement = new DtoMovementDocumentEntidadExterna(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> documentEntidadExternaService.registrarMovement(1, 999, movement))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Intentar completar la documentación de entidades externas")
    class IntentarCompletarDocumentacionTests {

        @Test
        @DisplayName("Transiciona la gestión a Documentacion Completa cuando todos los documents quedan entregados")
        void shouldTransitionManagementWhenAllDocumentsDelivered() {
            document.setDelivered(true);
            when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            documentEntidadExternaService.intentarCompletarDocumentacion(1);

            ArgumentCaptor<Integer> idManagementCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
            verify(managementTransitionService, times(1)).transition(idManagementCaptor.capture(), statusCaptor.capture());
            assertThat(idManagementCaptor.getValue()).isEqualTo(1);
            assertThat(statusCaptor.getValue()).isEqualTo(BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        }

        @Test
        @DisplayName("No transiciona la gestión cuando quedan documents sin entregar")
        void shouldNotTransitionWhenDocumentsPending() {
            when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            documentEntidadExternaService.intentarCompletarDocumentacion(1);

            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("No propaga la excepción si el workflow no admite la transición automática")
        void shouldSwallowBusinessValidationExceptionOnAutoTransition() {
            document.setDelivered(true);
            when(submittedDocumentRepository.findByFkIdProcedureFkIdManagementIdManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));
            when(managementTransitionService.transition(eq(1), eq(BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA)))
                    .thenThrow(new BusinessValidationException("Transición no permitida"));

            documentEntidadExternaService.intentarCompletarDocumentacion(1);

            verify(managementTransitionService, times(1)).transition(1, BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        }
    }
}
