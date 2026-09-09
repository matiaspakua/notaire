package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.business.DocumentCostTemplate;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DocumentCostTemplateRepository;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.service.DocumentCostTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlantillaCostoDocumentoService Unit Tests (Issue #823)")
class DocumentCostTemplateServiceTest {

    @Mock
    private DocumentCostTemplateRepository documentCostTemplateRepository;

    @Mock
    private ProcedureTypeRepository procedureTypeRepository;

    @Mock
    private DocumentTypeRepository documentTypeRepository;

    @InjectMocks
    private DocumentCostTemplateService documentCostTemplateService;

    private ProcedureType procedureType;
    private DocumentType documentType;

    @BeforeEach
    void setUp() {
        procedureType = new ProcedureType();
        procedureType.setIdProcedureType(1);

        documentType = new DocumentType();
        documentType.setIdDocumentType(1);
    }

    @Test
    @DisplayName("Should accept fixed cost for type de documento")
    void shouldAcceptFixedCostForTypeDocument() {
        when(procedureTypeRepository.findById(1)).thenReturn(Optional.of(procedureType));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        when(documentCostTemplateRepository.save(any(DocumentCostTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentCostTemplate result = documentCostTemplateService.create(1, 1, 1500f, null);

        assertThat(result.getFixedAmount()).isEqualTo(1500f);
        assertThat(result.getVariablePercentage()).isNull();
    }

    @Test
    @DisplayName("Should accept variable cost for type de documento")
    void shouldAcceptVariableCostForTypeDocument() {
        when(procedureTypeRepository.findById(1)).thenReturn(Optional.of(procedureType));
        when(documentTypeRepository.findById(1)).thenReturn(Optional.of(documentType));
        when(documentCostTemplateRepository.save(any(DocumentCostTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentCostTemplate result = documentCostTemplateService.create(1, 1, null, 5f);

        assertThat(result.getVariablePercentage()).isEqualTo(5f);
        assertThat(result.getFixedAmount()).isNull();
    }

    @Test
    @DisplayName("Should reject when both fixed and variable cost provided")
    void shouldRejectWhenBothFixedAndVariableCostProvided() {
        assertThatThrownBy(() -> documentCostTemplateService.create(1, 1, 1500f, 5f))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("exactamente uno");
    }

    @Test
    @DisplayName("Should reject when neither fixed nor variable cost provided")
    void shouldRejectWhenNeitherFixedNorVariableCostProvided() {
        assertThatThrownBy(() -> documentCostTemplateService.create(1, 1, null, null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("exactamente uno");
    }
}
