package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.property.PropertyLegacyMapper;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.dto.DtoProperty;

@DisplayName("Property mapping lives outside the entity")
class PropertyLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyPropertyFieldsOntoTheDto")
    void shouldCopyPropertyFieldsOntoTheDto() {
        Property property = new Property();
        property.setIdProperty(10);
        property.setAddress("Address");
        property.setCadastralDesignation("N-1");
        property.setNotes("notes");
        property.setFiscalAppraisal(new BigDecimal("10000"));
        property.setRegistrationNumber("R-1");
        property.setVolumeFolioLandRecord("V-1");
        property.setBoundaries("north");

        DtoProperty dto = PropertyLegacyMapper.toDto(property);

        assertThat(dto.getIdProperty()).isEqualTo(10);
        assertThat(dto.getAddress()).isEqualTo("Address");
        assertThat(dto.getCadastralDesignation()).isEqualTo("N-1");
        assertThat(dto.getNotes()).isEqualTo("notes");
        assertThat(dto.getFiscalAppraisal()).isEqualByComparingTo("10000");
        assertThat(dto.getRegistrationNumber()).isEqualTo("R-1");
        assertThat(dto.getVolumeFolioLandRecord()).isEqualTo("V-1");
        assertThat(dto.getBoundaries()).isEqualTo("north");
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfProperty")
    void shouldKeepDtoTypesOutOfProperty() {
        assertThat(Arrays.stream(Property.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
