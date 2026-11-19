package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.testimony.TestimonyMovementLegacyMapper;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoTestimonyMovement;

@DisplayName("Testimony movement mapping lives outside the entity")
class TestimonyMovementLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyMovementFieldsOntoTheDto")
    void shouldCopyMovementFieldsOntoTheDto() {
        Date entered = new Date(1_700_000_000_000L);
        Testimony testimony = new Testimony(99);
        TestimonyMovement movement = new TestimonyMovement();
        movement.setIdTestimonyMovement(10);
        movement.setDateEntry(entered);
        movement.setDateRegistration(entered);
        movement.setDateExit(entered);
        movement.setRegistered(true);
        movement.setCardNumber(5);
        movement.setNotes("notes");
        movement.setObservedByRegistry(true);
        movement.setVersion(1);
        movement.setTestimony(testimony);

        DtoTestimonyMovement dto = TestimonyMovementLegacyMapper.toDto(movement);

        assertThat(dto.getIdTestimonyMovement()).isEqualTo(10);
        assertThat(dto.getDateEntry()).isEqualTo(entered);
        assertThat(dto.getCardNumber()).isEqualTo(5);
        assertThat(dto.getNotes()).isEqualTo("notes");
        assertThat(dto.isRegistered()).isTrue();
        assertThat(dto.isObservedByRegistry()).isTrue();
        assertThat(dto.getTestimony().getIdTestimony()).isEqualTo(99);
    }

    @Test
    @DisplayName("shouldApplyDtoFieldsOntoAMovement")
    void shouldApplyDtoFieldsOntoAMovement() {
        DtoTestimony testimony = new DtoTestimony();
        testimony.setIdTestimony(99);
        DtoTestimonyMovement dto = new DtoTestimonyMovement();
        dto.setIdTestimonyMovement(50);
        dto.setCardNumber(1);
        dto.setNotes("o");
        dto.setVersion(2);
        dto.setRegistered(true);
        dto.setTestimony(testimony);

        TestimonyMovement movement = new TestimonyMovement();
        TestimonyMovementLegacyMapper.apply(movement, dto);

        assertThat(movement.getIdTestimonyMovement()).isEqualTo(50);
        assertThat(movement.getCardNumber()).isEqualTo(1);
        assertThat(movement.getNotes()).isEqualTo("o");
        assertThat(movement.getVersion()).isEqualTo(2);
        assertThat(movement.getTestimony().getIdTestimony()).isEqualTo(99);
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfTestimonyMovement")
    void shouldKeepDtoTypesOutOfTestimonyMovement() {
        assertThat(Arrays.stream(TestimonyMovement.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto", "setAttributes");
    }
}
