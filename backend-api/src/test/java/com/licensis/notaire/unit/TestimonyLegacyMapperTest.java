package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.testimony.TestimonyLegacyMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoTestimony;
import com.licensis.notaire.dto.DtoTestimonyMovement;

class TestimonyLegacyMapperTest {

    @Test
    @DisplayName("toDto copies the testimony, a short deed, and each movement")
    void toDtoCopiesFields() {
        Testimony testimony = new Testimony();
        testimony.setIdTestimony(10);
        testimony.setNumber(5);
        testimony.setNotes("notes");
        testimony.setFlagged(true);
        testimony.setVerified(true);
        testimony.setVersion(3);
        Deed deed = new Deed();
        deed.setIdDeed(99);
        testimony.setFkIdDeed(deed);
        TestimonyMovement movement = new TestimonyMovement();
        movement.setIdTestimonyMovement(7);
        movement.setNotes("moved");
        movement.setTestimony(new Testimony(10));
        testimony.setTestimonyMovementList(java.util.List.of(movement));

        DtoTestimony dto = TestimonyLegacyMapper.toDto(testimony);

        assertThat(dto.getIdTestimony()).isEqualTo(10);
        assertThat(dto.getNumber()).isEqualTo(5);
        assertThat(dto.getNotes()).isEqualTo("notes");
        assertThat(dto.isFlagged()).isTrue();
        assertThat(dto.isVerified()).isTrue();
        assertThat(dto.getVersion()).isEqualTo(3);
        assertThat(dto.getDeed().getIdDeed()).isEqualTo(99);
        assertThat(dto.getTestimonyMovements()).hasSize(1);
        assertThat(dto.getTestimonyMovements().get(0).getNotes()).isEqualTo("moved");
        assertThat(dto.getTestimonyMovements().get(0).getTestimony().getIdTestimony()).isEqualTo(10);
    }

    @Test
    @DisplayName("apply copies id, deed, and movements and keeps a null id")
    void applyCopiesFields() {
        DtoTestimony dto = new DtoTestimony();
        dto.setNumber(1);
        dto.setNotes("o");
        dto.setFlagged(true);
        dto.setVerified(false);
        dto.setVersion(2);
        DtoDeed deed = new DtoDeed();
        deed.setIdDeed(99);
        dto.setDeed(deed);
        DtoTestimonyMovement movement = new DtoTestimonyMovement();
        movement.setIdTestimonyMovement(7);
        movement.setNotes("o");
        movement.setVersion(1);
        DtoTestimony nested = new DtoTestimony();
        nested.setIdTestimony(10);
        movement.setTestimony(nested);
        dto.setTestimonyMovements(java.util.List.of(movement));

        Testimony testimony = new Testimony();
        TestimonyLegacyMapper.apply(testimony, dto);

        assertThat(testimony.getIdTestimony()).isNull();
        assertThat(testimony.getNumber()).isEqualTo(1);
        assertThat(testimony.getNotes()).isEqualTo("o");
        assertThat(testimony.getFlagged()).isTrue();
        assertThat(testimony.getVersion()).isEqualTo(2);
        assertThat(testimony.getFkIdDeed().getIdDeed()).isEqualTo(99);
        assertThat(testimony.getTestimonyMovementList()).hasSize(1);
        assertThat(testimony.getTestimonyMovementList().get(0).getNotes()).isEqualTo("o");
        assertThat(testimony.getTestimonyMovementList().get(0).getTestimony().getIdTestimony()).isEqualTo(10);
    }

    @Test
    @DisplayName("the entity no longer exposes getDto or setAttributes")
    void entityDoesNotMapItself() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/licensis/notaire/business/Testimony.java"));

        assertThat(source).doesNotContain("getDto", "setAttributes");
    }
}
