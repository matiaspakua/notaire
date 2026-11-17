package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.document.DocumentTypeLegacyMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.dto.DtoDocumentType;

@DisplayName("DocumentType mapping lives outside the entity")
class DocumentTypeLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyDocumentTypeFieldsOntoTheDto")
    void shouldCopyDocumentTypeFieldsOntoTheDto() {
        DocumentType type = new DocumentType();
        type.setIdDocumentType(4);
        type.setName("DNI");
        type.setExpires(true);
        type.setDueDays(30);
        type.setDeliveredBy("registry");
        type.setVersion(2);
        type.setEnabled(true);
        type.setReturned(false);

        DtoDocumentType dto = DocumentTypeLegacyMapper.toDto(type);

        assertThat(dto.getIdDocumentType()).isEqualTo(4);
        assertThat(dto.getName()).isEqualTo("DNI");
        assertThat(dto.isExpires()).isTrue();
        assertThat(dto.getDueDays()).isEqualTo(30);
        assertThat(dto.getDeliveredBy()).isEqualTo("registry");
        assertThat(dto.getVersion()).isEqualTo(2);
        assertThat(dto.getEnabled()).isTrue();
        assertThat(dto.getReturned()).isFalse();
    }

    @Test
    @DisplayName("shouldOmitDueDaysWhenTheDocumentDoesNotExpire")
    void shouldOmitDueDaysWhenTheDocumentDoesNotExpire() {
        DocumentType type = new DocumentType();
        type.setExpires(false);
        type.setDueDays(12);

        assertThat(DocumentTypeLegacyMapper.toDto(type).getDueDays()).isNull();
    }

    @Test
    @DisplayName("shouldApplyDtoFieldsAndClearDueDaysWhenItDoesNotExpire")
    void shouldApplyDtoFieldsAndClearDueDaysWhenItDoesNotExpire() {
        DtoDocumentType dto = new DtoDocumentType();
        dto.setIdDocumentType(99);
        dto.setName("RG");
        dto.setExpires(false);
        dto.setDueDays(9);
        dto.setDeliveredBy("u");
        dto.setEnabled(true);
        dto.setVersion(0);

        DocumentType type = new DocumentType();
        type.setDueDays(9);
        DocumentTypeLegacyMapper.apply(type, dto);

        assertThat(type.getIdDocumentType()).isEqualTo(99);
        assertThat(type.getName()).isEqualTo("RG");
        assertThat(type.getExpires()).isFalse();
        assertThat(type.getDueDays()).isNull();
        assertThat(type.getDeliveredBy()).isEqualTo("u");
        assertThat(type.getEnabled()).isTrue();
        assertThat(type.getVersion()).isEqualTo(0);
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfDocumentType")
    void shouldKeepDtoTypesOutOfDocumentType() {
        assertThat(Arrays.stream(DocumentType.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto", "setAttributes");
    }
}
