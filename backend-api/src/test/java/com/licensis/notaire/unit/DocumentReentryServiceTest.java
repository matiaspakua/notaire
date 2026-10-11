package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementDocumentReentry;
import com.licensis.notaire.dto.DtoDocumentReentryRequest;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureTemplatePK;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.application.usecase.workflow.DocumentReentryService;
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
@DisplayName("DocumentReentryService Tests")
@ExtendWith(MockitoExtension.class)
class DocumentReentryServiceTest {

    @Mock
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ProcedureTemplateRepository procedureTemplateRepository;

    @Mock
    private SubmittedDocumentRepository submittedDocumentRepository;

    private DocumentReentryService service;

    private DeedManagement management;
    private ProcedureType typeProcedure;
    private Procedure procedure;
    private DocumentType typeDocument;

    @BeforeEach
    void setUp() {
        service = new DocumentReentryService(managementRepository, procedureRepository,
                procedureTemplateRepository, submittedDocumentRepository);

        management = new DeedManagement();
        management.setIdManagement(1);
        management.setNumber(100);
        management.setHeading("Compraventa");

        typeProcedure = new ProcedureType();
        typeProcedure.setIdProcedureType(5);
        typeProcedure.setName("Compraventa");

        procedure = new Procedure();
        procedure.setIdProcedure(10);
        procedure.setFkIdManagement(management);
        procedure.setFkIdProcedureType(typeProcedure);

        typeDocument = new DocumentType();
        typeDocument.setIdDocumentType(7);
        typeDocument.setName("Certificado de Dominio");
        typeDocument.setExpires(true);
        typeDocument.setDueDays(30);
        typeDocument.setDeliveredBy("Cliente");
    }

    @Nested
    @DisplayName("Required documentation of a management")
    class RequiredDocumentationTests {

        @Test
        @DisplayName("Returns the management procedures with their required documentation")
        void shouldReturnProceduresWithDocumentacionNecesaria() {
            ProcedureTemplate template = new ProcedureTemplate(new ProcedureTemplatePK(5, 7));
            template.setDocumentType(typeDocument);
            template.setProcedureType(typeProcedure);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of(template));

            DtoManagementDocumentReentry result = service.getRequiredDocumentation(1);

            assertThat(result.idManagement()).isEqualTo(1);
            assertThat(result.number()).isEqualTo(100);
            assertThat(result.procedures()).hasSize(1);
            assertThat(result.procedures().get(0).idProcedure()).isEqualTo(10);
            assertThat(result.procedures().get(0).typeProcedureName()).isEqualTo("Compraventa");
            assertThat(result.procedures().get(0).documentsNecesarios()).hasSize(1);
            assertThat(result.procedures().get(0).documentsNecesarios().get(0).name())
                    .isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Returns empty required documentation when the procedure has no template")
        void shouldReturnEmptyDocumentationWhenNoTemplate() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of());

            DtoManagementDocumentReentry result = service.getRequiredDocumentation(1);

            assertThat(result.procedures()).hasSize(1);
            assertThat(result.procedures().get(0).documentsNecesarios()).isEmpty();
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the management does not exist")
        void shouldThrowWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRequiredDocumentation(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Re-enter a document type")
    class ReenterTests {

        @Test
        @DisplayName("Creates a submitted document with reentered=true from the document type")
        void shouldCreateSubmittedDocumentWhenValid() {
            ProcedureTemplate template = new ProcedureTemplate(new ProcedureTemplatePK(5, 7));
            template.setDocumentType(typeDocument);
            template.setProcedureType(typeProcedure);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(10)).thenReturn(Optional.of(procedure));
            when(procedureTemplateRepository.findById(new ProcedureTemplatePK(5, 7))).thenReturn(Optional.of(template));
            when(submittedDocumentRepository.save(any(SubmittedDocument.class))).thenAnswer(invocation -> {
                SubmittedDocument doc = invocation.getArgument(0);
                doc.setIdSubmittedDocument(50);
                return doc;
            });

            DtoDocumentReentryRequest request = new DtoDocumentReentryRequest(10, 7);
            DtoDocumentReentered result = service.reenter(1, request);

            assertThat(result.idSubmittedDocument()).isEqualTo(50);
            assertThat(result.idProcedure()).isEqualTo(10);
            assertThat(result.idDocumentType()).isEqualTo(7);
            assertThat(result.name()).isEqualTo("Certificado de Dominio");
            assertThat(result.expires()).isTrue();
            assertThat(result.dueDays()).isEqualTo(30);
            assertThat(result.deliveredBy()).isEqualTo("Cliente");
            assertThat(result.reentered()).isTrue();

            ArgumentCaptor<SubmittedDocument> captor = ArgumentCaptor.forClass(SubmittedDocument.class);
            org.mockito.Mockito.verify(submittedDocumentRepository).save(captor.capture());
            assertThat(captor.getValue().getReentered()).isTrue();
            assertThat(captor.getValue().getFkIdProcedure()).isSameAs(procedure);
        }

        @Test
        @DisplayName("Throws BusinessValidationException when the document type is not in the template")
        void shouldThrowWhenTypeDocumentNotInTemplate() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(10)).thenReturn(Optional.of(procedure));
            when(procedureTemplateRepository.findById(new ProcedureTemplatePK(5, 7))).thenReturn(Optional.empty());

            DtoDocumentReentryRequest request = new DtoDocumentReentryRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Throws BusinessValidationException when the procedure does not belong to the management")
        void shouldThrowWhenProcedureDoesNotBelongToManagement() {
            DeedManagement otraManagement = new DeedManagement();
            otraManagement.setIdManagement(2);
            procedure.setFkIdManagement(otraManagement);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(10)).thenReturn(Optional.of(procedure));

            DtoDocumentReentryRequest request = new DtoDocumentReentryRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the procedure does not exist")
        void shouldThrowWhenProcedureNotFound() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(999)).thenReturn(Optional.empty());

            DtoDocumentReentryRequest request = new DtoDocumentReentryRequest(999, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when the management does not exist")
        void shouldThrowWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            DtoDocumentReentryRequest request = new DtoDocumentReentryRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(999, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
