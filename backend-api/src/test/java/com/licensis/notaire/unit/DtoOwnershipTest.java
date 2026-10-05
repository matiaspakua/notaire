package com.licensis.notaire.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO ownership - classes shipped by backend-api")
class DtoOwnershipTest {

    private static final String BACKEND_CLASSES = "backend-api/target/classes";

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
        "com.licensis.notaire.dto.DtoPerson",
        "com.licensis.notaire.dto.TypeItem",
        "com.licensis.notaire.dto.exceptions.DtoInvalidoException",
        "com.licensis.notaire.jpa.exceptions.PreexistingEntityException"
    })
    @DisplayName("should load the class from the backend-api build output")
    void shouldLoadClassFromBackendApiBuildOutput(String className) throws ClassNotFoundException {
        Class<?> type = Class.forName(className);

        String location = type.getProtectionDomain().getCodeSource().getLocation().getPath();

        assertThat(location).contains(BACKEND_CLASSES);
    }
}
