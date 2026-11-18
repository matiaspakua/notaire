package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.deed.DeedLegacyMapper;
import com.licensis.notaire.business.Deed;

@DisplayName("Deed mapping lives outside the entity")
class DeedLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyDeedFieldsOntoTheDto")
    void shouldCopyDeedFieldsOntoTheDto() {
        Date recorded = new Date(1_700_000_000_000L);
        Date registered = new Date(1_700_100_000_000L);
        Deed deed = new Deed(8);
        deed.setNumber(3001);
        deed.setBody("body");
        deed.setStatus("OPEN");
        deed.setDateDeedrecording(recorded);
        deed.setDateRegistration(registered);
        deed.setRegistrationEntryNumber("E-9");
        deed.setVersion(3);

        var dto = DeedLegacyMapper.toDto(deed);

        assertThat(dto.getIdDeed()).isEqualTo(8);
        assertThat(dto.getNumber()).isEqualTo(3001);
        assertThat(dto.getBody()).isEqualTo("body");
        assertThat(dto.getStatus()).isEqualTo("OPEN");
        assertThat(dto.getDateDeedrecording()).isEqualTo(recorded);
        assertThat(dto.getDateRegistration()).isEqualTo(registered);
        assertThat(dto.getRegistrationEntryNumber()).isEqualTo("E-9");
        assertThat(dto.getVersion()).isEqualTo(3);
        assertThat(dto.getProcedures()).isNull();
        assertThat(dto.getFolios()).isEmpty();
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfDeed")
    void shouldKeepDtoTypesOutOfDeed() {
        assertThat(Arrays.stream(Deed.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
