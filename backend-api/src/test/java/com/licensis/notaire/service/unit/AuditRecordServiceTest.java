package com.licensis.notaire.service.unit;

import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.AuditRecordRepository;
import com.licensis.notaire.service.AuditRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistroAuditoriaService Unit Tests")
class AuditRecordServiceTest {

    @Mock
    private AuditRecordRepository repository;

    @InjectMocks
    private AuditRecordService service;

    private AuditRecord testRecord;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setIdUser(1);
        testUser.setName("Juan");
        testUser.setType("ADMIN");
        testUser.setStatus(true);

        testRecord = new AuditRecord();
        testRecord.setIdAuditRecord(1);
        testRecord.setModule("DOCUMENTOS");
        testRecord.setOperationDetail("CREATE");
        testRecord.setDate(new Date());
        testRecord.setFkIdUser(testUser);
    }

    @Test
    @DisplayName("Should find all registros auditoria")
    void shouldFindAll() {
        List<AuditRecord> registros = new ArrayList<>();
        registros.add(testRecord);

        when(repository.findAll()).thenReturn(registros);

        List<AuditRecord> result = service.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRecord);

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no registros exist")
    void shouldReturnEmptyListWhenNoRegistrosExist() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        List<AuditRecord> result = service.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find all registros as DTOs")
    void shouldFindAllAsDto() {
        List<AuditRecord> registros = new ArrayList<>();
        registros.add(testRecord);

        when(repository.findAllWithUser()).thenReturn(registros);

        List<DtoAuditRecord> result = service.findAllAsDto();

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModule().equals("DOCUMENTOS"));

        verify(repository, times(1)).findAllWithUser();
    }

    @Test
    @DisplayName("Should find registros with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<AuditRecord> result = service.findAll(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRecord);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find registros as DTOs with pagination")
    void shouldFindAllAsPagedDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(page);

        Page<DtoAuditRecord> result = service.findAllAsDto(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModule().equals("DOCUMENTOS"));

        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find registros by module")
    void shouldFindByModule() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findByModule("DOCUMENTOS", pageable)).thenReturn(page);

        Page<AuditRecord> result = service.findByModule("DOCUMENTOS", pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(reg -> reg.getModule().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByModule("DOCUMENTOS", pageable);
    }

    @Test
    @DisplayName("Should find registros by module as DTOs")
    void shouldFindByModuleAsDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findByModule("DOCUMENTOS", pageable)).thenReturn(page);

        Page<DtoAuditRecord> result = service.findByModuleAsDto("DOCUMENTOS", pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModule().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByModule("DOCUMENTOS", pageable);
    }

    @Test
    @DisplayName("Should find registros by usuario id")
    void shouldFindByUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findByFkIdUserIdUser(1, pageable)).thenReturn(page);

        Page<AuditRecord> result = service.findByUserId(1, pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(reg -> reg.getFkIdUser().getIdUser().equals(1));

        verify(repository, times(1)).findByFkIdUserIdUser(1, pageable);
    }

    @Test
    @DisplayName("Should find registros by usuario id as DTOs")
    void shouldFindByUserIdAsDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);

        when(repository.findByFkIdUserIdUser(1, pageable)).thenReturn(page);

        Page<DtoAuditRecord> result = service.findByUserIdAsDto(1, pageable);

        assertThat(result).isNotNull()
                .hasSize(1);

        verify(repository, times(1)).findByFkIdUserIdUser(1, pageable);
    }

    @Test
    @DisplayName("Should find registro by id")
    void shouldFindById() {
        when(repository.findById(1)).thenReturn(Optional.of(testRecord));

        Optional<AuditRecord> result = service.findById(1);

        assertThat(result).isPresent()
                .contains(testRecord);

        verify(repository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when registro not found")
    void shouldReturnEmptyOptionalWhenRecordNotFound() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        Optional<AuditRecord> result = service.findById(999);

        assertThat(result).isEmpty();

        verify(repository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should find registro by id as DTO")
    void shouldFindByIdAsDto() {
        when(repository.findByIdWithUser(1)).thenReturn(Optional.of(testRecord));

        Optional<DtoAuditRecord> result = service.findByIdAsDto(1);

        assertThat(result).isPresent()
                .hasValueSatisfying(dto -> {
                    assertThat(dto.getIdAuditRecord()).isEqualTo(1);
                    assertThat(dto.getModule()).isEqualTo("DOCUMENTOS");
                });

        verify(repository, times(1)).findByIdWithUser(1);
    }

    @Test
    @DisplayName("Should find registros list by usuario id")
    void shouldFindRegistrosByUserIdList() {
        List<AuditRecord> registros = new ArrayList<>();
        registros.add(testRecord);

        when(repository.findByFkIdUserIdUser(1)).thenReturn(registros);

        List<AuditRecord> result = service.findByUserId(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testRecord);

        verify(repository, times(1)).findByFkIdUserIdUser(1);
    }

    @Test
    @DisplayName("Should find registros list by usuario id as DTOs")
    void shouldFindRegistrosByUserIdAsDto() {
        List<AuditRecord> registros = new ArrayList<>();
        registros.add(testRecord);

        when(repository.findByUserIdWithUser(1)).thenReturn(registros);

        List<DtoAuditRecord> result = service.findByUserIdAsDto(1);

        assertThat(result).isNotNull()
                .hasSize(1)
                .allMatch(dto -> dto.getModule().equals("DOCUMENTOS"));

        verify(repository, times(1)).findByUserIdWithUser(1);
    }

    @Test
    @DisplayName("Should save registro auditoria")
    void shouldSaveRecord() {
        when(repository.save(testRecord)).thenReturn(testRecord);

        AuditRecord result = service.save(testRecord);

        assertThat(result).isNotNull()
                .isEqualTo(testRecord)
                .extracting(AuditRecord::getModule)
                .isEqualTo("DOCUMENTOS");

        verify(repository, times(1)).save(testRecord);
    }

    @Test
    @DisplayName("Should map registro to DTO correctly")
    void shouldMapRecordToDto() {
        DtoAuditRecord dto = AuditRecordService.toDto(testRecord);

        assertThat(dto).isNotNull()
                .extracting(DtoAuditRecord::getIdAuditRecord,
                        DtoAuditRecord::getModule,
                        DtoAuditRecord::getOperationDetail)
                .containsExactly(1, "DOCUMENTOS", "CREATE");
        assertThat(dto.getUsers()).isNotNull()
                .extracting(u -> u.getName())
                .isEqualTo("Juan");
    }

    @Test
    @DisplayName("Should handle null usuario in DTO mapping")
    void shouldHandleNullUserInDtoMapping() {
        AuditRecord recordSinUser = new AuditRecord();
        recordSinUser.setIdAuditRecord(1);
        recordSinUser.setModule("DOCUMENTOS");
        recordSinUser.setOperationDetail("DELETE");
        recordSinUser.setDate(new Date());
        recordSinUser.setFkIdUser(null);

        DtoAuditRecord dto = AuditRecordService.toDto(recordSinUser);

        assertThat(dto).isNotNull()
                .extracting(DtoAuditRecord::getModule)
                .isEqualTo("DOCUMENTOS");
        assertThat(dto.getUsers()).isNull();
    }
}
