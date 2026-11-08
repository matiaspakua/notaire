package com.licensis.notaire.adapter.in.web.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.out.item.ItemOpsPort.ItemView;
import com.licensis.notaire.dto.TypeItem;

/**
 * Wire mapping for items must not take JPA entities (issue #1826 / ArchUnit R1).
 */
@DisplayName("ItemResponse mapping (issue #1826)")
class ItemResponseMappingTest {

    @Test
    @DisplayName("Should map from ItemView without depending on business.Item")
    void shouldMapFromItemView() {
        ItemView view = new ItemView(
                3,
                "Stamp duty",
                new BigDecimal("500"),
                0,
                "note",
                TypeItem.NORMAL,
                null,
                false,
                9,
                1);

        ItemResponse response = ItemResponse.from(view);

        assertThat(response.idItem()).isEqualTo(3);
        assertThat(response.name()).isEqualTo("Stamp duty");
        assertThat(response.budgetId()).isEqualTo(9);
        assertThat(response.version()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should expose only from(ItemView) — no from(Item) overload")
    void shouldNotExposeFromItemEntity() {
        Method[] methods = ItemResponse.class.getDeclaredMethods();
        assertThat(Arrays.stream(methods)
                        .filter(m -> m.getName().equals("from") && m.getParameterCount() == 1)
                        .map(m -> m.getParameterTypes()[0].getName()))
                .containsExactly("com.licensis.notaire.application.port.out.item.ItemOpsPort$ItemView");
    }
}
