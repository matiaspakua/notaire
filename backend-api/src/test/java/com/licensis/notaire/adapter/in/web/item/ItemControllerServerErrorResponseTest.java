package com.licensis.notaire.adapter.in.web.item;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.item.ManageItemsUseCase;
import com.licensis.notaire.application.port.in.item.QueryItemsUseCase;
import com.licensis.notaire.application.port.out.item.ItemOpsPort.SaveResult;
import com.licensis.notaire.config.GlobalExceptionHandler;

@DisplayName("ItemController ServerError ErrorResponse (issue #579)")
class ItemControllerServerErrorResponseTest {

    private final ManageItemsUseCase manage = mock(ManageItemsUseCase.class);
    private final MockMvc mvc = standaloneSetup(new ItemController(mock(QueryItemsUseCase.class), manage))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    private static final String BODY = "{\"name\":\"Test item\",\"value\":1000.0}";

    @Test
    @DisplayName("create ServerError returns 500 ErrorResponse body (#579)")
    void createServerErrorReturnsErrorResponseBody() throws Exception {
        when(manage.create(any())).thenReturn(new SaveResult.ServerError());

        mvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
