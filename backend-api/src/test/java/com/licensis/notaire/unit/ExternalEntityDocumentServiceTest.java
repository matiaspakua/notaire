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
        notary.setLastName("Notary");

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
        document.setDeliveredBy(BusinessConstants.DocumentationExternalEntity);
        document.setFkIdProcedure(procedure);
        document.setDelivered(false);
    }

    @Nested
    @DisplayName("Get external-entity documents of a management")
    class GetDocumentsTests {

        @Test
        @DisplayName("Returns the management with its external-entity documents and cadastral designation")
        void shouldReturnManagementWithDocumentsAndDesignation() {
            Property property = new Property();
            property.setCadastralDesignation("12-34-56");
            procedure.setFkIdProperty(property);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findProceduresByManagementId(1)).thenReturn(List.of(procedure));
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DocumentationExternalEntity)).thenReturn(List.of(document));

            DtoManagementExternalEntityDocuments result = externalEntityDocumentService.getDocuments(1);

            assertThat(result.idManagement()).isEqualTo(1);
            assertThat(result.number()).isEqualTo(100);
            assertThat(result.notary()).isEqualTo("Ana Notary");
            assertThat(result.cadastralDesignation()).isEqualTo("12-34-56");
            assertThat(result.documents()).hasSize(1);
            assertThat(result.documents().get(0).name()).isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Cadastral nomenclature is null when no procedure has an associated property")
        void shouldReturnNullDesignationWhenNoProperty() {
            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findProceduresByManagementId(1)).thenReturn(List.of(procedure));
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DocumentationExternalEntity)).thenReturn(List.of());

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
    @DisplayName("Register a movement for an external-entity document")
    class RegisterMovementTests {

        @Test
        @DisplayName("Updates the document movement fields")
        void shouldUpdateMovementFields() {
            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));
            when(documentRepository.saveSubmittedDocument(any(SubmittedDocument.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Date dateEntry = new Date();
            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, 7, dateEntry, null, false, new java.math.BigDecimal("1500"), null, null, "Withdrawn on time", false);

            DtoExternalEntityDocument result =
                    externalEntityDocumentService.registerMovement(1, 50, movement);

            assertThat(result.prepared()).isTrue();
            assertThat(result.cardNumber()).isEqualTo(7);
            assertThat(result.dateEntry()).isEqualTo(dateEntry);
            assertThat(result.amountToPay()).isEqualByComparingTo(new java.math.BigDecimal("1500"));
            assertThat(result.notes()).isEqualTo("Withdrawn on time");
            assertThat(result.delivered()).isFalse();
            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("Throws BusinessValidationException when the document does not belong to the indicated management")
        void shouldThrowWhenDocumentDoesNotBelongToManagement() {
            DeedManagement otherManagement = new DeedManagement();
            otherManagement.setIdManagement(2);
            Procedure otherProcedure = new Procedure();
            otherProcedure.setIdProcedure(20);
            otherProcedure.setFkIdManagement(otherManagement);
            document.setFkIdProcedure(otherProcedure);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));

            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> externalEntityDocumentService.registerMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Throws BusinessValidationException when the document is not for an external entity")
        void shouldThrowWhenDocumentIsNotExternalEntity() {
            document.setDeliveredBy(BusinessConstants.DocumentationClient);

            when(documentRepository.findManagementById(1)).thenReturn(Optional.of(management));
            when(documentRepository.findSubmittedDocumentById(50)).thenReturn(Optional.of(document));

            DtoExternalEntityDocumentMovement movement = new DtoExternalEntityDocumentMovement(
                    true, null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> externalEntityDocumentService.registerMovement(1, 50, movement))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the document does not exist")
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
    @DisplayName("Attempt to complete the external-entity documentation")
    class TryCompleteDocumentationTests {

        @Test
        @DisplayName("Transitions the management to Documentacion Completa when every document is delivered")
        void shouldTransitionManagementWhenAllDocumentsDelivered() {
            document.setDelivered(true);
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DocumentationExternalEntity)).thenReturn(List.of(document));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            ArgumentCaptor<Integer> idManagementCaptor = ArgumentCaptor.forClass(Integer.class);
            ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
            verify(managementTransitionService, times(1)).transition(idManagementCaptor.capture(), statusCaptor.capture());
            assertThat(idManagementCaptor.getValue()).isEqualTo(1);
            assertThat(statusCaptor.getValue()).isEqualTo(BusinessConstants.ManagementWithCompleteDocumentation);
        }

        @Test
        @DisplayName("Does not transition the management when documents are still undelivered")
        void shouldNotTransitionWhenDocumentsPending() {
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DocumentationExternalEntity)).thenReturn(List.of(document));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            verify(managementTransitionService, never()).transition(anyInt(), any());
        }

        @Test
        @DisplayName("Does not propagate the exception when the workflow rejects the automatic transition")
        void shouldSwallowBusinessValidationExceptionOnAutoTransition() {
            document.setDelivered(true);
            when(documentRepository.findByManagementAndDeliveredBy(
                    1, BusinessConstants.DocumentationExternalEntity)).thenReturn(List.of(document));
            when(managementTransitionService.transition(eq(1), eq(BusinessConstants.ManagementWithCompleteDocumentation)))
                    .thenThrow(new BusinessValidationException("Transition is not allowed"));

            externalEntityDocumentService.tryCompleteDocumentation(1);

            verify(managementTransitionService, times(1)).transition(1, BusinessConstants.ManagementWithCompleteDocumentation);
        }
    }
}
