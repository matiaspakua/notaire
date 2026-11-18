package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.management.ManagementStatusLegacyMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.exceptions.InvalidDtoException;

@DisplayName("ManagementStatus mapping lives outside the entity")
class ManagementStatusLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyManagementStatusFieldsOntoTheDto")
    void shouldCopyManagementStatusFieldsOntoTheDto() throws Exception {
        ManagementStatus status = new ManagementStatus();
        status.setIdManagementStatus(4);
        status.setName("Iniciada");
        status.setNotes("n");
        status.setVersion(2);

        DtoManagementStatus dto = ManagementStatusLegacyMapper.toDto(status);

        assertThat(dto.getIdManagementStatus()).isEqualTo(4);
        assertThat(dto.getName()).isEqualTo("Iniciada");
        assertThat(dto.getNotes()).isEqualTo("n");
        assertThat(dto.getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("shouldRejectAnInvalidDto")
    void shouldRejectAnInvalidDto() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Keep");
        DtoManagementStatus dto = new DtoManagementStatus() {
            @Override
            public Boolean isValid() {
                return false;
            }
        };
        dto.setName("Drop");

        assertThatThrownBy(() -> ManagementStatusLegacyMapper.apply(status, dto))
                .isInstanceOf(InvalidDtoException.class);
        assertThat(status.getName()).isEqualTo("Keep");
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfManagementStatus")
    void shouldKeepDtoTypesOutOfManagementStatus() {
        assertThat(Arrays.stream(ManagementStatus.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto", "setAttribute");
    }
}
