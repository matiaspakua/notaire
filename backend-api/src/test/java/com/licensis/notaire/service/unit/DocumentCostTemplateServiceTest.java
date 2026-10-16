package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.adapter.out.persistence.documentcosttemplate.DocumentCostTemplateService;
import com.licensis.notaire.repository.DocumentCostTemplateRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentCostTemplateService Unit Tests (Issue #823)")
class DocumentCostTemplateServiceTest {

    @Mock
    private DocumentCostTemplateRepository costTemplateRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @Mock
    private ProcedureTypeRepository procedureTypeRepository;

    private DocumentCostTemplateService documentCostTemplateService;

    private ProcedureType procedureType;
    private DocumentType documentType;

    @BeforeEach
    void setUp() {
        documentCostTemplateService = new DocumentCostTemplateService(
                costTemplateRepository, documentTypeRepository, procedureTypeRepository);

        procedureType = new ProcedureType();
        procedureType.setIdProcedureType(1);

        documentType = new DocumentType();
        documentType.setIdDocumentType(1);
    }

    @Test
    @DisplayName("Should accept fixed cost for document type")
    void shouldAcceptFixedCostForTypeDocument() {
        when(procedureTypeRepository.findById(1)).thenReturn(Optional.of(procedureType));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        when(costTemplateRepository.save(any(DocumentCostTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentCostTemplate result = documentCostTemplateService.create(1, 1, new java.math.BigDecimal("1500"), null);

        assertThat(result.getFixedAmount()).isEqualByComparingTo(new java.math.BigDecimal("1500"));
        assertThat(result.getVariablePercentage()).isNull();
    }

    @Test
    @DisplayName("Should accept variable cost for document type")
    void shouldAcceptVariableCostForTypeDocument() {
        when(procedureTypeRepository.findById(1)).thenReturn(Optional.of(procedureType));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        when(costTemplateRepository.save(any(DocumentCostTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentCostTemplate result = documentCostTemplateService.create(1, 1, null, new java.math.BigDecimal("5"));

        assertThat(result.getVariablePercentage()).isEqualByComparingTo(new java.math.BigDecimal("5"));
        assertThat(result.getFixedAmount()).isNull();
    }

    @Test
    @DisplayName("Should reject when both fixed and variable cost provided")
    void shouldRejectWhenBothFixedAndVariableCostProvided() {
        assertThatThrownBy(() -> documentCostTemplateService.create(1, 1, new java.math.BigDecimal("1500"), new java.math.BigDecimal("5")))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Exactly one");
    }

    @Test
    @DisplayName("Should reject when neither fixed nor variable cost provided")
    void shouldRejectWhenNeitherFixedNorVariableCostProvided() {
        assertThatThrownBy(() -> documentCostTemplateService.create(1, 1, null, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Exactly one");
    }
}
