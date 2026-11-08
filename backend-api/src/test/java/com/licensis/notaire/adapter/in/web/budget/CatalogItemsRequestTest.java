package com.licensis.notaire.adapter.in.web.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BudgetController.CatalogItemsRequest — defensive copy (#1971)")
class CatalogItemsRequestTest {

    @Test
    @DisplayName("later changes to the caller's list do not leak into the request")
    void shouldCopyCallerList() {
        List<Integer> ids = new ArrayList<>(List.of(1, 2));
        BudgetController.CatalogItemsRequest request = new BudgetController.CatalogItemsRequest(ids);

        ids.add(3);

        assertThat(request.idItems()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("exposed list is unmodifiable")
    void shouldExposeUnmodifiableList() {
        BudgetController.CatalogItemsRequest request =
                new BudgetController.CatalogItemsRequest(new ArrayList<>(List.of(1)));

        assertThatThrownBy(() -> request.idItems().add(2)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("null list stays null so Bean Validation (@NotEmpty) reports it")
    void shouldKeepNullForValidation() {
        assertThat(new BudgetController.CatalogItemsRequest(null).idItems()).isNull();
    }
}
