package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("DTO classes that nothing uses are deleted")
class DtoRemovalTest {

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
        "com.licensis.notaire.dto.DtoFlag",
        "com.licensis.notaire.dto.DtoIdentification"
    })
    @DisplayName("shouldNotContainUnusedDtoClass")
    void shouldNotContainUnusedDtoClass(String className) {
        assertThatThrownBy(() -> Class.forName(className)).isInstanceOf(ClassNotFoundException.class);
    }
}
