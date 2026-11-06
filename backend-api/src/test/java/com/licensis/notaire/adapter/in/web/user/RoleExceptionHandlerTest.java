package com.licensis.notaire.adapter.in.web.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.licensis.notaire.application.port.in.user.ManageRolesUseCase;
import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.user.RoleNameConflictException;
import com.licensis.notaire.domain.user.RoleNotFoundException;

@DisplayName("RoleExceptionHandler maps domain exceptions (#577)")
class RoleExceptionHandlerTest {

    private final ManageRolesUseCase manage = mock(ManageRolesUseCase.class);
    private final MockMvc mvc = standaloneSetup(new RoleController(mock(QueryRolesUseCase.class), manage))
            .setControllerAdvice(new RoleExceptionHandler(), new GlobalExceptionHandler())
            .build();

    private static final String BODY =
            "{\"name\":\"ADMIN\",\"description\":\"x\",\"active\":true,\"modulos\":[]}";

    @Test
    @DisplayName("create RoleNameConflict returns 409 error body")
    void createConflictReturns409() throws Exception {
        when(manage.create(any())).thenThrow(new RoleNameConflictException("ADMIN"));

        mvc.perform(post("/api/v1/roles").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Already exists a role with the name: ADMIN"));
    }

    @Test
    @DisplayName("assign RoleNotFound returns 404")
    void assignMissingRoleReturns404() throws Exception {
        doThrow(new RoleNotFoundException(9)).when(manage).assignRoleToUser(9, 1);

        mvc.perform(put("/api/v1/roles/9/users/1")).andExpect(status().isNotFound());
    }
}
