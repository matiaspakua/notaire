package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.folio.FolioTypeLegacyMapper;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.dto.DtoFolioType;

@DisplayName("FolioType mapping lives outside the entity")
class FolioTypeLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyFolioTypeFieldsOntoTheDto")
    void shouldCopyFolioTypeFieldsOntoTheDto() {
        FolioType type = new FolioType();
        type.setIdFolioType(10);
        type.setName("Protocol");
        type.setNotes("notes");
        type.setEnabled(true);
        type.setIsAuxiliary(false);
        type.setVersion(3);

        DtoFolioType dto = FolioTypeLegacyMapper.toDto(type);

        assertThat(dto.getIdFolioType()).isEqualTo(10);
        assertThat(dto.getName()).isEqualTo("Protocol");
        assertThat(dto.getNotes()).isEqualTo("notes");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.isIsAuxiliary()).isFalse();
        assertThat(dto.getVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("shouldApplyDtoFieldsOntoTheEntity")
    void shouldApplyDtoFieldsOntoTheEntity() throws Exception {
        DtoFolioType dto = new DtoFolioType();
        dto.setIdFolioType(99);
        dto.setName("NewName");
        dto.setNotes("n");
        dto.setEnabled(false);
        dto.setIsAuxiliary(true);
        dto.setVersion(2);

        FolioType type = new FolioType();
        FolioTypeLegacyMapper.apply(type, dto);

        assertThat(type.getIdFolioType()).isEqualTo(99);
        assertThat(type.getName()).isEqualTo("NewName");
        assertThat(type.getNotes()).isEqualTo("n");
        assertThat(type.getEnabled()).isFalse();
        assertThat(type.isIsAuxiliary()).isTrue();
        assertThat(type.getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfFolioType")
    void shouldKeepDtoTypesOutOfFolioType() {
        assertThat(Arrays.stream(FolioType.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto", "setAttributes");
    }
}
