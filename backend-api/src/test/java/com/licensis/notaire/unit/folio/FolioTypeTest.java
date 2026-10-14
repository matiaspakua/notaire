package com.licensis.notaire.unit.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.domain.folio.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeInvalidException;

@DisplayName("FolioType domain model (issue #1703)")
class FolioTypeTest {

    @Test
    @DisplayName("create starts without an id at version 0 and keeps the flags")
    void createStartsNew() {
        FolioType type = FolioType.create("Protocol", "n", true, false);

        assertThat(type.id()).isNull();
        assertThat(type.version()).isZero();
        assertThat(type.name()).isEqualTo("Protocol");
        assertThat(type.enabled()).isTrue();
        assertThat(type.auxiliary()).isFalse();
    }

    @Test
    @DisplayName("a name is required: only a missing one is refused, as the database column rule did before")
    void nameRequired() {
        assertThatThrownBy(() -> FolioType.create(null, null, true, false)).isInstanceOf(FolioTypeInvalidException.class);
    }

    @Test
    @DisplayName("restore accepts a stored row without validating it")
    void restoreDoesNotValidate() {
        FolioType legacy = FolioType.restore(7, " ", null, false, false, 3);

        assertThat(legacy.id()).isEqualTo(7);
        assertThat(legacy.version()).isEqualTo(3);
    }

    @Test
    @DisplayName("changeTo keeps the identity and version and replaces the fields")
    void changeToKeepsIdentity() {
        FolioType changed = FolioType.restore(4, "Old", "o", true, false, 2).changeTo("New", null, false, true);

        assertThat(changed.id()).isEqualTo(4);
        assertThat(changed.version()).isEqualTo(2);
        assertThat(changed.name()).isEqualTo("New");
        assertThat(changed.notes()).isNull();
        assertThat(changed.enabled()).isFalse();
        assertThat(changed.auxiliary()).isTrue();
    }
}
