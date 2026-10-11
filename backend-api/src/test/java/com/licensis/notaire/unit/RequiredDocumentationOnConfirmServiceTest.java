package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.application.usecase.procedure.RequiredDocumentationOnConfirmService;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureTemplate;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.ProcedureTemplateRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU02", "CU03"})
@DisplayName("RequiredDocumentationOnConfirmService (#773)")
@ExtendWith(MockitoExtension.class)
class RequiredDocumentationOnConfirmServiceTest {

    @Mock
    private ProcedureTemplateRepository procedureTemplateRepository;

    @Mock
    private SubmittedDocumentRepository submittedDocumentRepository;

    private RequiredDocumentationOnConfirmService service;
    private Procedure procedure;

    @BeforeEach
    void setUp() {
        service = new RequiredDocumentationOnConfirmService(procedureTemplateRepository,
                submittedDocumentRepository);
        ProcedureType type = new ProcedureType();
        type.setIdProcedureType(5);
        procedure = new Procedure();
        procedure.setIdProcedure(10);
        procedure.setFkIdProcedureType(type);
    }

    private static ProcedureTemplate template(int id, String name, boolean expires, Integer dueDays) {
        DocumentType type = new DocumentType(id, name, expires, "CLIENT");
        type.setDueDays(dueDays);
        ProcedureTemplate template = new ProcedureTemplate();
        template.setDocumentType(type);
        return template;
    }

    @Test
    @DisplayName("shouldSeedPendingDocumentsFromTemplate")
    void shouldSeedPendingDocumentsFromTemplate() {
        when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5))
                .thenReturn(List.of(template(1, "ID card", true, 30), template(2, "Deed copy", false, null)));
        when(submittedDocumentRepository.findByFkIdProcedureIdProcedure(10)).thenReturn(List.of());

        int created = service.seedRequiredDocuments(procedure);

        assertThat(created).isEqualTo(2);
        ArgumentCaptor<SubmittedDocument> captor = ArgumentCaptor.forClass(SubmittedDocument.class);
        verify(submittedDocumentRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        SubmittedDocument first = captor.getAllValues().get(0);
        assertThat(first.getName()).isEqualTo("ID card");
        assertThat(first.getFkIdProcedure()).isSameAs(procedure);
        assertThat(first.getFkIdDocumentType()).isEqualTo(1);
        assertThat(first.getExpires()).isTrue();
        assertThat(first.getDueDays()).isEqualTo(30);
        assertThat(first.getDeliveredBy()).isEqualTo("CLIENT");
        assertThat(first.getDelivered()).isFalse();
        assertThat(first.getReentered()).isFalse();
        assertThat(first.getReleased()).isFalse();
        assertThat(first.getFlagged()).isFalse();
    }

    @Test
    @DisplayName("shouldSeedNothingWhenTemplateIsEmpty")
    void shouldSeedNothingWhenTemplateIsEmpty() {
        when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5)).thenReturn(List.of());

        assertThat(service.seedRequiredDocuments(procedure)).isZero();

        verify(submittedDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("shouldSkipDocumentTypesAlreadySubmittedForProcedure")
    void shouldSkipDocumentTypesAlreadySubmittedForProcedure() {
        when(procedureTemplateRepository.findByProcedureTypeIdProcedureType(5))
                .thenReturn(List.of(template(1, "ID card", true, 30), template(2, "Deed copy", false, null)));
        SubmittedDocument existing = new SubmittedDocument();
        existing.setDocumentType(new DocumentType(1, "ID card", true, "CLIENT"));
        when(submittedDocumentRepository.findByFkIdProcedureIdProcedure(10)).thenReturn(List.of(existing));

        int created = service.seedRequiredDocuments(procedure);

        assertThat(created).isEqualTo(1);
        ArgumentCaptor<SubmittedDocument> captor = ArgumentCaptor.forClass(SubmittedDocument.class);
        verify(submittedDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getFkIdDocumentType()).isEqualTo(2);
    }
}
