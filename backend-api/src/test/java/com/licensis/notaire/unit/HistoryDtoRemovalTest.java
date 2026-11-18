package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.business.History;

@DisplayName("History no longer builds a transport DTO")
class HistoryDtoRemovalTest {

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfHistory")
    void shouldKeepDtoTypesOutOfHistory() {
        assertThat(Arrays.stream(History.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
