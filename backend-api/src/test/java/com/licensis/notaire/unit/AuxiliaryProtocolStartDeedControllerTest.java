package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.notebook.AuxiliaryProtocolController;
import com.licensis.notaire.adapter.in.web.notebook.AuxiliaryProtocolStartDeedExceptionHandler;
import com.licensis.notaire.adapter.in.web.notebook.AuxiliaryProtocolStartDeedWebMapper;
import com.licensis.notaire.application.port.in.notebook.ManageAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.in.notebook.QueryAuxiliaryProtocolUseCase;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.DeedView;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuxiliaryProtocolController startDeed")
class AuxiliaryProtocolStartDeedControllerTest {

    @Mock
    private QueryAuxiliaryProtocolUseCase queryAuxiliaryProtocol;

    @Mock
    private ManageAuxiliaryProtocolUseCase manageAuxiliaryProtocol;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuxiliaryProtocolController(
                        queryAuxiliaryProtocol,
                        manageAuxiliaryProtocol,
                        new AuxiliaryProtocolStartDeedWebMapper()))
                .setControllerAdvice(new AuxiliaryProtocolStartDeedExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /auxiliary-protocol/deeds returns 201 with the deed body")
    void shouldStartDeed() throws Exception {
        var view = new DeedView(7, 3, "text", "ACTIVE", new Date(), null, null, null, 0);
        when(manageAuxiliaryProtocol.startDeed(eq(1), eq("text"), any())).thenReturn(new StartResult.Created(view));

        mockMvc.perform(post("/api/v1/auxiliary-protocol/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idFolio\":1,\"body\":\"text\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDeed").value(7))
                .andExpect(jsonPath("$.number").value(3));
    }

    @Test
    @DisplayName("POST /auxiliary-protocol/deeds returns 400 with a message body")
    void shouldReturnBadRequest() throws Exception {
        when(manageAuxiliaryProtocol.startDeed(eq(1), any(), any())).thenReturn(new StartResult.BadRequest("invalid"));

        mockMvc.perform(post("/api/v1/auxiliary-protocol/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idFolio\":1,\"body\":\"text\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid"));
    }

    @Test
    @DisplayName("POST /auxiliary-protocol/deeds returns 404 with a message body")
    void shouldReturnNotFound() throws Exception {
        when(manageAuxiliaryProtocol.startDeed(eq(1), any(), any())).thenReturn(new StartResult.NotFound("missing"));

        mockMvc.perform(post("/api/v1/auxiliary-protocol/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idFolio\":1,\"body\":\"text\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("missing"));
    }
}
