package com.licensis.notaire.unit.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.domain.management.ManagementStatus;
import com.licensis.notaire.domain.management.ManagementStatusInvalidException;

@DisplayName("ManagementStatus domain model (issue #1719)")
class ManagementStatusTest {

    @Test
    @DisplayName("create starts without an id at version 0")
    void createStartsNew() {
        ManagementStatus status = ManagementStatus.create("Started", "note");

        assertThat(status.id()).isNull();
        assertThat(status.version()).isZero();
        assertThat(status.name()).isEqualTo("Started");
        assertThat(status.notes()).isEqualTo("note");
    }

    @Test
    @DisplayName("a name is required: only a missing one is refused, as the database column rule did before")
    void nameRequired() {
        assertThatThrownBy(() -> ManagementStatus.create(null, "n"))
                .isInstanceOf(ManagementStatusInvalidException.class);
    }

    @Test
    @DisplayName("restore accepts a stored row without validating it")
    void restoreDoesNotValidate() {
        ManagementStatus legacy = ManagementStatus.restore(7, " ", null, 3);

        assertThat(legacy.id()).isEqualTo(7);
        assertThat(legacy.version()).isEqualTo(3);
        assertThat(legacy.name()).isEqualTo(" ");
        assertThat(legacy.notes()).isNull();
    }

    @Test
    @DisplayName("changeTo keeps the identity and version and replaces the fields")
    void changeToKeepsIdentity() {
        ManagementStatus changed = ManagementStatus.restore(4, "Old", "A", 2).changeTo("New", "B");

        assertThat(changed.id()).isEqualTo(4);
        assertThat(changed.version()).isEqualTo(2);
        assertThat(changed.name()).isEqualTo("New");
        assertThat(changed.notes()).isEqualTo("B");
    }
}
