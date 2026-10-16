package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.adapter.in.web.audit.AuditRecordController;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort;
import com.licensis.notaire.application.port.out.audit.AuditRecordOpsPort.PageResult;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.AuditRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.tags.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuditRecordController unit tests")
@ExtendWith(MockitoExtension.class)
class AuditRecordControllerTest {

    @Mock
    private AuditRecordOpsPort ops;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DtoAuditRecord sampleDto;
    private AuditRecord sampleEntity;

    @BeforeEach
    void setUp() {
        AuditRecordController controller = new AuditRecordController(ops);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        DtoPerson person = new DtoPerson();
        person.setId(10);
        person.setFirstName("Admin");
        person.setLastName("System");

        DtoUser user = new DtoUser();
        user.setIdUser(1);
        user.setName("admin");
        user.setType("Escribano");
        user.setStatus(true);
        user.setPersons(person);

        sampleDto = new DtoAuditRecord();
        sampleDto.setIdAuditRecord(100);
        sampleDto.setOperationDetail("Listing query of deeds");
        sampleDto.setModule("Deeds");
        sampleDto.setDate(new Date());
        sampleDto.setUsers(user);

        sampleEntity = new AuditRecord();
        sampleEntity.setIdAuditRecord(100);
        sampleEntity.setOperationDetail("Listing query of deeds");
        sampleEntity.setModule("Deeds");
        sampleEntity.setDate(new Date());
    }

    private PageResult pageOf(DtoAuditRecord dto) {
        return new PageResult(List.of(dto), 0, 20, 1, 1, true, true);
    }

    @Test
    @DisplayName("Should return a list of DTOs without pagination params")
    void shouldReturnFlatListWhenNoPaginationParams() throws Exception {
        when(ops.findAll()).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAuditRecord").value(100))
                .andExpect(jsonPath("$[0].module").value("Deeds"))
                .andExpect(jsonPath("$[0].operationDetail").value("Listing query of deeds"))
                .andExpect(jsonPath("$[0].users.name").value("admin"))
                .andExpect(jsonPath("$[0].users.persons.firstName").value("Admin"));
    }

    @Test
    @DisplayName("Should return a paginated response when page parameter is provided")
    void shouldReturnPagedResponseWhenPageProvided() throws Exception {
        when(ops.search(isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), anyString())).thenReturn(pageOf(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idAuditRecord").value(100))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("Should filter by module when module param is set with pagination")
    void shouldFilterByModuleWhenParamSet() throws Exception {
        when(ops.search(eq("Deeds"), isNull(), isNull(), isNull(), eq(0), eq(20), anyString())).thenReturn(pageOf(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&module=Deeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].module").value("Deeds"));

        verify(ops).search(eq("Deeds"), isNull(), isNull(), isNull(), eq(0), eq(20), anyString());
        verify(ops, never()).findAll();
    }

    @Test
    @DisplayName("Should filter by idUser when idUser param is set with pagination")
    void shouldFilterByIdUserWhenParamSet() throws Exception {
        when(ops.search(isNull(), eq(1), isNull(), isNull(), eq(0), eq(20), anyString())).thenReturn(pageOf(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&idUser=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].users.idUser").value(1));

        verify(ops).search(isNull(), eq(1), isNull(), isNull(), eq(0), eq(20), anyString());
    }

    @Test
    @DisplayName("Should return the audit record by id when found")
    void shouldReturnRecordByIdWhenFound() throws Exception {
        when(ops.findById(100)).thenReturn(Optional.of(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAuditRecord").value(100));
    }

    @Test
    @DisplayName("Should return 404 when the audit record id is not found")
    void shouldReturn404WhenIdNotFound() throws Exception {
        when(ops.findById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/audit-log/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return audit records by user id")
    void shouldReturnRecordsByUserId() throws Exception {
        when(ops.findByUserId(1)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].users.idUser").value(1));
    }

    @Test
    @DisplayName("Should reject create — audit trail is append-only via AuditoriaAspect (#1068/#1060)")
    void shouldRejectCreateOfAuditRecords() throws Exception {
        mockMvc.perform(post("/api/v1/audit-log")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleEntity)))
                .andExpect(status().isMethodNotAllowed());
        verify(ops, never()).findAll();
    }

    @Test
    @DisplayName("Should reject update — audit trail is immutable over HTTP (#1060)")
    void shouldRejectUpdateOfAuditRecords() throws Exception {
        mockMvc.perform(put("/api/v1/audit-log/100")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleEntity)))
                .andExpect(status().isMethodNotAllowed());
        verify(ops, never()).findById(anyInt());
    }

    @Test
    @DisplayName("Should reject deletion of audit records - the trail is append-only (issue #556)")
    void shouldRejectDeleteOfAuditRecords() throws Exception {
        mockMvc.perform(delete("/api/v1/audit-log/100"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("OpenAPI tag describes consult-only access (#1060)")
    void shouldDescribeConsultOnlyOpenApiTag() {
        Tag tag = AuditRecordController.class.getAnnotation(Tag.class);
        assertThat(tag).isNotNull();
        assertThat(tag.description())
                .as("OpenAPI must not advertise administration of audit records")
                .doesNotContainIgnoringCase("administrar")
                .containsIgnoringCase("querying");
    }

    @Test
    @DisplayName("Should fall back to default sort on invalid sort parameter")
    void shouldFallBackToDefaultSortOnInvalidParameter() throws Exception {
        when(ops.search(isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), anyString())).thenReturn(pageOf(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&sort=date,unknown"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept negative page or zero size and normalize")
    void shouldNormalizeInvalidPaginationValues() throws Exception {
        when(ops.search(isNull(), isNull(), isNull(), isNull(), eq(0), eq(20), anyString())).thenReturn(pageOf(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log?page=-1&size=0"))
                .andExpect(status().isOk());
    }
}
