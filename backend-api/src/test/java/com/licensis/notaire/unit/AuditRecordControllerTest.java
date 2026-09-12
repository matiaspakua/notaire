package com.licensis.notaire.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.api.AuditRecordController;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.service.AuditRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("RegistroAuditoriaController unit tests")
@ExtendWith(MockitoExtension.class)
class AuditRecordControllerTest {

    @Mock
    private AuditRecordService service;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DtoAuditRecord sampleDto;
    private AuditRecord sampleEntity;

    @BeforeEach
    void setUp() {
        AuditRecordController controller = new AuditRecordController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();

        DtoPerson person = new DtoPerson();
        person.setId(10);
        person.setFirstName("Admin");
        person.setLastName("Sistema");

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

    @Test
    @DisplayName("Should return a list of DTOs without pagination params")
    void shouldReturnFlatListWhenNoPaginationParams() throws Exception {
        when(service.findAllAsDto()).thenReturn(List.of(sampleDto));

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
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date"));
        Page<DtoAuditRecord> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

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
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date"));
        Page<DtoAuditRecord> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findByModuleAsDto(eq("Deeds"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&module=Deeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].module").value("Deeds"));

        verify(service).findByModuleAsDto(eq("Deeds"), any(Pageable.class));
        verify(service, never()).findAllAsDto(any(Pageable.class));
    }

    @Test
    @DisplayName("Should filter by idUser when idUser param is set with pagination")
    void shouldFilterByIdUserWhenParamSet() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date"));
        Page<DtoAuditRecord> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findByUserIdAsDto(eq(1), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&idUser=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].users.idUser").value(1));

        verify(service).findByUserIdAsDto(eq(1), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return registro by id when found")
    void shouldReturnRecordByIdWhenFound() throws Exception {
        when(service.findByIdAsDto(100)).thenReturn(Optional.of(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAuditRecord").value(100));
    }

    @Test
    @DisplayName("Should return 404 when registro id not found")
    void shouldReturn404WhenIdNotFound() throws Exception {
        when(service.findByIdAsDto(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/audit-log/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return registros by usuario id")
    void shouldReturnRegistrosByUserId() throws Exception {
        when(service.findByUserIdAsDto(1)).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/v1/audit-log/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].users.idUser").value(1));
    }

    @Test
    @DisplayName("Should create a new registro and return its entity")
    void shouldCreateNewRecordAndReturnEntity() throws Exception {
        when(service.save(any(AuditRecord.class))).thenReturn(sampleEntity);

        mockMvc.perform(post("/api/v1/audit-log")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sampleEntity)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAuditRecord").value(100));
    }

    @Test
    @DisplayName("Should reject deletion of audit records - the trail is append-only (issue #556)")
    void shouldRejectDeleteOfAuditRecords() throws Exception {
        mockMvc.perform(delete("/api/v1/audit-log/100"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Should fall back to default sort on invalid sort parameter")
    void shouldFallBackToDefaultSortOnInvalidParameter() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date"));
        Page<DtoAuditRecord> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit-log?page=0&size=20&sort=date,unknown"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should accept negative page or zero size and normalize")
    void shouldNormalizeInvalidPaginationValues() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "date"));
        Page<DtoAuditRecord> page = new PageImpl<>(List.of(sampleDto), pageable, 1);
        when(service.findAllAsDto(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/audit-log?page=-1&size=0"))
                .andExpect(status().isOk());
    }
}
