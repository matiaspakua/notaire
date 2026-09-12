package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDocumentReentered;
import com.licensis.notaire.dto.DtoManagementReingresoDocumentacion;
import com.licensis.notaire.dto.DtoReingresoDocumentacionRequest;
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
    private DeedManagementRepository managementRepository;

    @Mock
    private ProcedureRepository procedureRepository;

    @Mock
    private ProcedureTemplateRepository procedureTemplateRepository;

    @Mock
    private SubmittedDocumentRepository submittedDocumentRepository;

    private ReingresoDocumentacionService service;

    private DeedManagement management;
    private ProcedureType typeProcedure;
    private Procedure procedure;
    private DocumentType typeDocument;

    @BeforeEach
    void setUp() {
        service = new ReingresoDocumentacionService(managementRepository, procedureRepository,
                procedureTemplateRepository, submittedDocumentRepository);

        management = new DeedManagement();
        management.setIdManagement(1);
        management.setNumber(100);
        management.setEncabezado("Compraventa");

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
    @DisplayName("Obtener documentación necesaria de una gestión")
    class ObtenerDocumentacionNecesariaTests {

        @Test
        @DisplayName("Devuelve los trámites de la gestión con su documentación necesaria")
        void shouldReturnProceduresWithDocumentacionNecesaria() {
            ProcedureTemplate template = new ProcedureTemplate(new ProcedureTemplatePK(5, 7));
            template.setDocumentType(typeDocument);
            template.setProcedureType(typeProcedure);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of(template));

            DtoManagementReingresoDocumentacion resultado = service.getRequiredDocumentation(1);

            assertThat(resultado.idManagement()).isEqualTo(1);
            assertThat(resultado.number()).isEqualTo(100);
            assertThat(resultado.procedures()).hasSize(1);
            assertThat(resultado.procedures().get(0).idProcedure()).isEqualTo(10);
            assertThat(resultado.procedures().get(0).typeProcedureName()).isEqualTo("Compraventa");
            assertThat(resultado.procedures().get(0).documentsNecesarios()).hasSize(1);
            assertThat(resultado.procedures().get(0).documentsNecesarios().get(0).name())
                    .isEqualTo("Certificado de Dominio");
        }

        @Test
        @DisplayName("Devuelve documentación necesaria vacía cuando el trámite no tiene PlantillaTramite")
        void shouldReturnEmptyDocumentacionWhenNoTemplate() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findByFkIdManagementIdManagement(1)).thenReturn(List.of(procedure));
            when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of());

            DtoManagementReingresoDocumentacion resultado = service.getRequiredDocumentation(1);

            assertThat(resultado.procedures()).hasSize(1);
            assertThat(resultado.procedures().get(0).documentsNecesarios()).isEmpty();
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRequiredDocumentation(999))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reingresar un type de documento")
    class ReingresarTests {

        @Test
        @DisplayName("Crea un DocumentoPresentado con reentered=true heredando datos del TipoDeDocumento")
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

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);
            DtoDocumentReentered resultado = service.reenter(1, request);

            assertThat(resultado.idSubmittedDocument()).isEqualTo(50);
            assertThat(resultado.idProcedure()).isEqualTo(10);
            assertThat(resultado.idDocumentType()).isEqualTo(7);
            assertThat(resultado.name()).isEqualTo("Certificado de Dominio");
            assertThat(resultado.expires()).isTrue();
            assertThat(resultado.dueDays()).isEqualTo(30);
            assertThat(resultado.deliveredBy()).isEqualTo("Cliente");
            assertThat(resultado.reentered()).isTrue();

            ArgumentCaptor<SubmittedDocument> captor = ArgumentCaptor.forClass(SubmittedDocument.class);
            org.mockito.Mockito.verify(submittedDocumentRepository).save(captor.capture());
            assertThat(captor.getValue().getReentered()).isTrue();
            assertThat(captor.getValue().getFkIdProcedure()).isSameAs(procedure);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el type de documento no forma parte de la PlantillaTramite")
        void shouldThrowWhenTypeDocumentNotInTemplate() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(10)).thenReturn(Optional.of(procedure));
            when(procedureTemplateRepository.findById(new ProcedureTemplatePK(5, 7))).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza BusinessValidationException cuando el trámite no pertenece a la gestión")
        void shouldThrowWhenProcedureDoesNotBelongToManagement() {
            DeedManagement otraManagement = new DeedManagement();
            otraManagement.setIdManagement(2);
            procedure.setFkIdManagement(otraManagement);

            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(10)).thenReturn(Optional.of(procedure));

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(BusinessValidationException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el trámite no existe")
        void shouldThrowWhenProcedureNotFound() {
            when(managementRepository.findById(1)).thenReturn(Optional.of(management));
            when(procedureRepository.findById(999)).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(999, 7);

            assertThatThrownBy(() -> service.reenter(1, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando la gestión no existe")
        void shouldThrowWhenManagementNotFound() {
            when(managementRepository.findById(999)).thenReturn(Optional.empty());

            DtoReingresoDocumentacionRequest request = new DtoReingresoDocumentacionRequest(10, 7);

            assertThatThrownBy(() -> service.reenter(999, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
