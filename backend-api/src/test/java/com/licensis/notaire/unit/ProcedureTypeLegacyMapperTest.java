package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.procedure.ProcedureTypeLegacyMapper;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.dto.DtoProcedureType;

@DisplayName("ProcedureType mapping lives outside the entity")
class ProcedureTypeLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyProcedureTypeFieldsOntoTheDto")
    void shouldCopyProcedureTypeFieldsOntoTheDto() {
        ProcedureType type = new ProcedureType();
        type.setIdProcedureType(4);
        type.setName("Escritura");
        type.setIsArchived(true);
        type.setIsRegistered(false);
        type.setAssociatesProperties(true);
        type.setNotes("n");
        type.setVersion(2);
        type.setEnabled(true);

        DtoProcedureType dto = ProcedureTypeLegacyMapper.toDto(type);

        assertThat(dto.getIdProcedureType()).isEqualTo(4);
        assertThat(dto.getName()).isEqualTo("Escritura");
        assertThat(dto.getIsArchived()).isTrue();
        assertThat(dto.getIsRegistered()).isFalse();
        assertThat(dto.getAssociatesProperties()).isTrue();
        assertThat(dto.getNotes()).isEqualTo("n");
        assertThat(dto.getVersion()).isEqualTo(2);
        assertThat(dto.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("shouldLeaveTheEntityUnchangedWhenTheDtoIsInvalid")
    void shouldLeaveTheEntityUnchangedWhenTheDtoIsInvalid() {
        ProcedureType type = new ProcedureType();
        type.setName("Keep");
        DtoProcedureType dto = new DtoProcedureType() {
            @Override
            public Boolean isValid() {
                return false;
            }
        };
        dto.setName("Drop");

        ProcedureTypeLegacyMapper.apply(type, dto);

        assertThat(type.getName()).isEqualTo("Keep");
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfProcedureType")
    void shouldKeepDtoTypesOutOfProcedureType() {
        assertThat(Arrays.stream(ProcedureType.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto", "setAttributes");
    }
}
