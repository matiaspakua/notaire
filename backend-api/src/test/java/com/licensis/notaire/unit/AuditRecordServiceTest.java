package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.AuditRecordRepository;
import com.licensis.notaire.service.AuditRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegistroAuditoriaService unit tests - CU23")
@ExtendWith(MockitoExtension.class)
class AuditRecordServiceTest {

    @Mock
    private AuditRecordRepository repository;

    @InjectMocks
    private AuditRecordService service;

    private AuditRecord testRecord;
    private User testUser;

    @BeforeEach
    void setUp() {
        Person person = new Person();
        person.setPersonId(10);
        person.setFirstName("Test");
        person.setLastName("User");

        testUser = new User();
        testUser.setIdUser(1);
        testUser.setName("testuser");
        testUser.setType("Escribano");
        testUser.setStatus(true);
        testUser.setFkIdPerson(person);

        testRecord = new AuditRecord();
        testRecord.setIdAuditRecord(1);
        testRecord.setFkIdUser(testUser);
        testRecord.setDate(new Date());
        testRecord.setOperationDetail("LOGIN");
        testRecord.setModule("USUARIOS");
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Should return all audit records as entities")
        void shouldReturnAllRegistros() {
            when(repository.findAll()).thenReturn(List.of(testRecord));

            List<AuditRecord> result = service.findAll();

            assertThat(result).hasSize(1).containsExactly(testRecord);
            verify(repository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no records exist")
        void shouldReturnEmptyListWhenNoneExist() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            assertThat(service.findAll()).isEmpty();
        }

        @Test
        @DisplayName("Should return paged audit records")
        void shouldReturnPagedRegistros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(page);

            Page<AuditRecord> result = service.findAll(pageable);

            assertThat(result.getContent()).containsExactly(testRecord);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return all audit records as DTOs")
        void shouldReturnAllAsDto() {
            when(repository.findAllWithUser()).thenReturn(List.of(testRecord));

            List<DtoAuditRecord> result = service.findAllAsDto();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdAuditRecord()).isEqualTo(1);
            assertThat(result.get(0).getUsers().getName()).isEqualTo("testuser");
            assertThat(result.get(0).getUsers().getPersons().getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should return paged audit records as DTOs")
        void shouldReturnPagedAsDto() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(page);

            Page<DtoAuditRecord> result = service.findAllAsDto(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getModule()).isEqualTo("USUARIOS");
        }
    }

    @Nested
    @DisplayName("findByModulo")
    class FindByModule {

        @Test
        @DisplayName("Should return audit records filtered by module")
        void shouldReturnRecordsForModule() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findByModule("USUARIOS", pageable)).thenReturn(page);

            Page<AuditRecord> result = service.findByModule("USUARIOS", pageable);

            assertThat(result.getContent()).containsExactly(testRecord);
        }

        @Test
        @DisplayName("Should return audit DTOs filtered by module")
        void shouldReturnDtosForModule() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findByModule("USUARIOS", pageable)).thenReturn(page);

            Page<DtoAuditRecord> result = service.findByModuleAsDto("USUARIOS", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getModule()).isEqualTo("USUARIOS");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Should return registro when found by ID")
        void shouldReturnRecordWhenFound() {
            when(repository.findById(1)).thenReturn(Optional.of(testRecord));

            Optional<AuditRecord> result = service.findById(1);

            assertThat(result).isPresent().contains(testRecord);
        }

        @Test
        @DisplayName("Should return empty when registro not found")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findById(999)).thenReturn(Optional.empty());

            assertThat(service.findById(999)).isEmpty();
        }

        @Test
        @DisplayName("Should return DTO when found by ID")
        void shouldReturnDtoWhenFound() {
            when(repository.findByIdWithUser(1)).thenReturn(Optional.of(testRecord));

            Optional<DtoAuditRecord> result = service.findByIdAsDto(1);

            assertThat(result).isPresent();
            assertThat(result.get().getModule()).isEqualTo("USUARIOS");
        }

        @Test
        @DisplayName("Should return empty DTO when not found")
        void shouldReturnEmptyDtoWhenNotFound() {
            when(repository.findByIdWithUser(999)).thenReturn(Optional.empty());

            assertThat(service.findByIdAsDto(999)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUsuarioId - CU23")
    class FindByUserId {

        @Test
        @DisplayName("Should return audit records for given user ID")
        void shouldReturnRecordsForUserId() {
            when(repository.findByFkIdUserIdUser(1)).thenReturn(List.of(testRecord));

            List<AuditRecord> result = service.findByUserId(1);

            assertThat(result).hasSize(1).containsExactly(testRecord);
            verify(repository).findByFkIdUserIdUser(1);
        }

        @Test
        @DisplayName("Should return empty list when user has no audit records")
        void shouldReturnEmptyListForUserWithNoRecords() {
            when(repository.findByFkIdUserIdUser(999)).thenReturn(Collections.emptyList());

            assertThat(service.findByUserId(999)).isEmpty();
        }

        @Test
        @DisplayName("Should return paged audit records for user")
        void shouldReturnPagedRecordsForUser() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findByFkIdUserIdUser(1, pageable)).thenReturn(page);

            Page<AuditRecord> result = service.findByUserId(1, pageable);

            assertThat(result.getContent()).containsExactly(testRecord);
        }

        @Test
        @DisplayName("Should return paged DTOs for user")
        void shouldReturnPagedDtosForUser() {
            Pageable pageable = PageRequest.of(0, 5);
            Page<AuditRecord> page = new PageImpl<>(List.of(testRecord), pageable, 1);
            when(repository.findByFkIdUserIdUser(1, pageable)).thenReturn(page);

            Page<DtoAuditRecord> result = service.findByUserIdAsDto(1, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUsers().getIdUser()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return DTOs for user without pagination")
        void shouldReturnDtosForUserUnpaged() {
            when(repository.findByUserIdWithUser(1)).thenReturn(List.of(testRecord));

            List<DtoAuditRecord> result = service.findByUserIdAsDto(1);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Should save and return audit record")
        void shouldSaveAndReturnRecord() {
            when(repository.save(any(AuditRecord.class))).thenReturn(testRecord);

            AuditRecord result = service.save(testRecord);

            assertThat(result).isEqualTo(testRecord);
            assertThat(result.getOperationDetail()).isEqualTo("LOGIN");
            verify(repository).save(testRecord);
        }
    }

    @Nested
    @DisplayName("toDto mapping")
    class ToDto {

        @Test
        @DisplayName("Should map entity to DTO with usuario and person")
        void shouldMapEntityWithUserAndPerson() {
            DtoAuditRecord dto = AuditRecordService.toDto(testRecord);

            assertThat(dto.getIdAuditRecord()).isEqualTo(1);
            assertThat(dto.getUsers().getName()).isEqualTo("testuser");
            assertThat(dto.getUsers().getPersons().getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should map entity to DTO when usuario is null")
        void shouldMapEntityWithoutUser() {
            testRecord.setFkIdUser(null);
            DtoAuditRecord dto = AuditRecordService.toDto(testRecord);

            assertThat(dto.getUsers()).isNull();
        }

        @Test
        @DisplayName("Should map entity to DTO when person is null")
        void shouldMapEntityWithoutPerson() {
            testUser.setFkIdPerson(null);
            DtoAuditRecord dto = AuditRecordService.toDto(testRecord);

            assertThat(dto.getUsers().getName()).isEqualTo("testuser");
            assertThat(dto.getUsers().getPersons()).isNull();
        }
    }
}
