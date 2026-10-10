package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.exception.BusinessValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Issue #655: one 400 that names every missing field of an incomplete body. */
class RequiredFieldsTest {

    @Test
    @DisplayName("all missing fields are named in one message, in the order they were checked")
    void namesEveryMissingField() {
        assertThatThrownBy(() -> RequiredFields.check()
                .present(false, "number")
                .present(true, "notes")
                .notNull(null, "verified")
                .orThrow())
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("number: es obligatorio; verified: es obligatorio");
    }

    @Test
    @DisplayName("a complete body passes")
    void completeBodyPasses() {
        assertThatCode(() -> RequiredFields.check().present(true, "number").notNull(1, "id").orThrow())
                .doesNotThrowAnyException();
    }
}
