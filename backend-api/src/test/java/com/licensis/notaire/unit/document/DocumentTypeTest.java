package com.licensis.notaire.unit.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.domain.document.DocumentType;
import com.licensis.notaire.domain.document.DocumentTypeInvalidException;

@DisplayName("DocumentType domain model (issue #1705)")
class DocumentTypeTest {

    @Test
    @DisplayName("create starts without an id at version 0 and clears dueDays when it does not expire")
    void createStartsNew() {
        DocumentType type = DocumentType.create("ID card", false, 30, "CLIENT", true, false);

        assertThat(type.id()).isNull();
        assertThat(type.version()).isZero();
        assertThat(type.name()).isEqualTo("ID card");
        assertThat(type.expires()).isFalse();
        assertThat(type.dueDays()).isNull();
        assertThat(type.deliveredBy()).isEqualTo("CLIENT");
        assertThat(type.enabled()).isTrue();
        assertThat(type.returned()).isFalse();
    }

    @Test
    @DisplayName("a name is required: only a missing one is refused, as the database column rule did before")
    void nameRequired() {
        assertThatThrownBy(() -> DocumentType.create(null, false, null, "", true, false))
                .isInstanceOf(DocumentTypeInvalidException.class);
    }

    @Test
    @DisplayName("restore accepts a stored row without validating it")
    void restoreDoesNotValidate() {
        DocumentType legacy = DocumentType.restore(7, " ", true, 10, null, false, true, 3);

        assertThat(legacy.id()).isEqualTo(7);
        assertThat(legacy.version()).isEqualTo(3);
        assertThat(legacy.name()).isEqualTo(" ");
    }

    @Test
    @DisplayName("changeTo keeps the identity and version and replaces the fields")
    void changeToKeepsIdentity() {
        DocumentType changed = DocumentType.restore(4, "Old", false, null, "A", true, false, 2)
                .changeTo("New", true, 15, "B", false, true);

        assertThat(changed.id()).isEqualTo(4);
        assertThat(changed.version()).isEqualTo(2);
        assertThat(changed.name()).isEqualTo("New");
        assertThat(changed.expires()).isTrue();
        assertThat(changed.dueDays()).isEqualTo(15);
        assertThat(changed.deliveredBy()).isEqualTo("B");
        assertThat(changed.enabled()).isFalse();
        assertThat(changed.returned()).isTrue();
    }
}
