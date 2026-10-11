package com.licensis.notaire.unit;


import com.licensis.notaire.application.port.out.document.ExternalDocumentRepositoryPort;
import com.licensis.notaire.dto.DtoExternalEntityDocument;
import com.licensis.notaire.dto.DtoManagementExternalEntityDocuments;
import com.licensis.notaire.dto.DtoExternalEntityDocumentMovement;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.application.usecase.document.ExternalEntityDocumentService;
import com.licensis.notaire.application.usecase.management.ManagementTransitionService;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("ExternalEntityDocumentService tests")
@ExtendWith(MockitoExtension.class)
class ExternalEntityDocumentServiceTest {

    @Mock
    private ExternalDocumentRepositoryPort documentRepository;

    @Mock
    private ManagementTransitionService managementTransitionService;

    private ExternalEntityDocumentService externalEntityDocumentService;

    private DeedManagement management;
    private Procedure procedure;
    private SubmittedDocument document;

    @BeforeEach
    void setUp() {
        externalEntityDocumentService = new ExternalEntityDocumentService(documentRepository,
            managementTransitionService);

        Person notary = new Person();
        notary.setFirstName("Ana");
        notary.setLastName("Notaria");

        management = new DeedManagement();
        management.setIdManagement(1);
        management.setNumber(100);
        management.setHeading("Compraventa");
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
    @DisplayName("Get documents of entidades externas of a management")
    class ObtenerDocumentsTests {

        @Test
        @DisplayName("Devuelve the management with their documents of external entity and cadastral nomenclature")
        void shouldReturnManagementWithDocumentsAndDesignation() {
            Property property = new Property();
            property.setCadastralDesignation("12-34-56");
            procedure.setFkIdProperty(property);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findProceduresByManagementId(1)).thenReturn(List.of(procedure));
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            DtoManagementExternalEntityDocuments result = externalEntityDocumentService.getDocuments(1);

            assertThat(result.idManagement()).isEqualTo(1);
            assertThat(result.number()).isEqualTo(100);
            assertThat(result.notary()).isEqualTo("Ana Notaria");
            assertThat(result.cadastralDesignation()).isEqualTo("12-34-56");
            assertThat(result.documents()).hasSize(1);
            assertThat(result.documents().get(0).name()).isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Cadastral nomenclature is null when ningún procedure has property associated")
        void shouldReturnNullDesignationWhenNoProperty() {
            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findProceduresByManagementId(1)).thenReturn(List.of(procedure));
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of());

            DtoManagementExternalEntityDocuments result = externalEntityDocumentService.getDocuments(1);

            assertThat(result.cadastralDesignation()).isNull();
            assertThat(result.documents()).isEmpty();
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the management does not exist")
        void shouldThrowWhenManagementNotFound() {
            when(documentRepository.findManagementById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> externalEntityDocumentService.getDocuments(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Registrar movimiento de un documento de entidad externa")
    class RegistrarMovementTests {

        @Test
        @DisplayName("Actualiza the campos of movimiento of the document")
        void shouldUpdateMovementFields() {
            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));
            when(documentRepository.saveSubmittedDocument(any(SubmittedDocument.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Date dateEntry = new Date();
            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, 7, dateEntry, null, false, new java.math.BigDecimal("1500"), null, null, "Retirado a tiempo", false);

            DtoExternalEntityDocument result =
                    externalEntityDocumentService.registerMovement(1, 50, movement);

            assertThat(result.prepared()).isTrue();
            assertThat(result.cardNumber()).isEqualTo(7);
            assertThat(result.dateEntry()).isEqualTo(dateEntry);
            assertThat(result.amountToPay()).isEqualByComparingTo(new java.math.BigDecimal("1500"));
            assertThat(result.notes()).isEqualTo("Retirado a tiempo");
            assertThat(result.delivered()).isFalse();
            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("Lanza BusinessValidationException when the document not belongs to the management indicada")
        void shouldThrowWhenDocumentDoesNotBelongToManagement() {
            DeedManagement otraManagement = new DeedManagement();
            otraManagement.setIdManagement(2);
            Procedure otroProcedure = new Procedure();
            otroProcedure.setIdProcedure(20);
            otroProcedure.setFkIdManagement(otraManagement);
            document.setFkIdProcedure(otroProcedure);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));

            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> externalEntityDocumentService.registerMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el documento no es de entidad externa")
        void shouldThrowWhenDocumentIsNotEntidadExterna() {
            document.setDeliveredBy(BusinessConstants.DOCUMENTACIONClient);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));

            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> externalEntityDocumentService.registerMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el documento no existe")
        void shouldThrowWhenDocumentNotFound() {
            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(999)).thenReturn(Optional.empty());

            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> externalEntityDocumentService.registerMovement(1, 999, movement))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Intentar completar the documentation of entidades externas")
    class IntentarCompletarDocumentacionTests {

        @Test
        @DisplayName("Transitions the management to Documentacion Completa when every document is delivered")
        void shouldTransitionManagementWhenAllDocumentsDelivered() {
            document.setDelivered(true);
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            ArgumentCaptor<Integer> idManagementCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
            verify(managementTransitionService, times(1)).transition(idManagementCaptor.capture(), statusCaptor.capture());
            assertThat(idManagementCaptor.getValue()).isEqualTo(1);
            assertThat(statusCaptor.getValue()).isEqualTo(BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        }

        @Test
        @DisplayName("Does not transition the management when documents are still undelivered")
        void shouldNotTransitionWhenDocumentsPending() {
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("Does not propagate the exception when the workflow rejects the automatic transition")
        void shouldSwallowBusinessValidationExceptionOnAutoTransition() {
            document.setDelivered(true);
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DOCUMENTACION_ENTIDAD_EXTERNA)).thenReturn(List.of(document));
            when(managementTransitionService.transition(eq(1), eq(BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA)))
                    .thenThrow(new BusinessValidationException("Transition is not allowed"));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            verify(managementTransitionService, times(1)).transition(1, BusinessConstants.ManagementCONDOCUMENTACIONCOMPLETA);
        }
    }
}
