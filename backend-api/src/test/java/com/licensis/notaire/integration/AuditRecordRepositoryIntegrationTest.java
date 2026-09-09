package com.licensis.notaire.integration;

import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.AuditRecordRepository;
import com.licensis.notaire.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegistroAuditoriaRepository Integration Tests")
class AuditRecordRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @Autowired
    private UserRepository userRepository;

    private AuditRecord testRecord;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Admin User");
        testUser.setType("ADMIN");
        testUser.setStatus(true);
        testUser.setPassword("password123");
        userRepository.save(testUser);

        testRecord = new AuditRecord();
        testRecord.setModule("DOCUMENTOS");
        testRecord.setOperationDetail("CREATE");
        testRecord.setDate(new Date());
        testRecord.setFkIdUser(testUser);
    }

    @Test
    @DisplayName("Should persist and retrieve registro auditoria")
    void shouldPersistAndRetrieveRecord() {
        AuditRecord saved = auditRecordRepository.save(testRecord);

        assertThat(saved.getIdAuditRecord()).isNotNull();

        Optional<AuditRecord> retrieved = auditRecordRepository.findById(saved.getIdAuditRecord());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getModule()).isEqualTo("DOCUMENTOS");
                    assertThat(r.getOperationDetail()).isEqualTo("CREATE");
                });
    }

    @Test
    @DisplayName("Should find registros by module")
    void shouldFindByModule() {
        auditRecordRepository.save(testRecord);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> found = auditRecordRepository.findByModule("DOCUMENTOS", pageable);

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getModule().equals("DOCUMENTOS"));
    }

    @Test
    @DisplayName("Should find registros by usuario id")
    void shouldFindByUserId() {
        auditRecordRepository.save(testRecord);

        List<AuditRecord> found = auditRecordRepository.findByFkIdUserIdUser(testUser.getIdUser());

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getFkIdUser().getIdUser().equals(testUser.getIdUser()));
    }

    @Test
    @DisplayName("Should find registros by usuario id paginated")
    void shouldFindByUserIdPaginated() {
        auditRecordRepository.save(testRecord);

        AuditRecord registro2 = new AuditRecord();
        registro2.setModule("USUARIOS");
        registro2.setOperationDetail("UPDATE");
        registro2.setDate(new Date());
        registro2.setFkIdUser(testUser);
        auditRecordRepository.save(registro2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditRecord> found = auditRecordRepository.findByFkIdUserIdUser(testUser.getIdUser(), pageable);

        assertThat(found).hasSize(2)
                .allMatch(r -> r.getFkIdUser().getIdUser().equals(testUser.getIdUser()));
    }

    @Test
    @DisplayName("Should find all registros with usuario")
    void shouldFindAllWithUser() {
        auditRecordRepository.save(testRecord);

        List<AuditRecord> all = auditRecordRepository.findAllWithUser();

        assertThat(all).isNotEmpty()
                .anyMatch(r -> r.getIdAuditRecord().equals(testRecord.getIdAuditRecord()));
    }

    @Test
    @DisplayName("Should find registro by id with usuario")
    void shouldFindByIdWithUser() {
        AuditRecord saved = auditRecordRepository.save(testRecord);

        Optional<AuditRecord> retrieved = auditRecordRepository.findByIdWithUser(saved.getIdAuditRecord());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getFkIdUser()).isNotNull();
                    assertThat(r.getFkIdUser().getIdUser()).isEqualTo(testUser.getIdUser());
                });
    }

    @Test
    @DisplayName("Should find registros by usuario id with usuario")
    void shouldFindByUserIdWithUser() {
        auditRecordRepository.save(testRecord);

        List<AuditRecord> found = auditRecordRepository.findByUserIdWithUser(testUser.getIdUser());

        assertThat(found).isNotEmpty()
                .allMatch(r -> r.getFkIdUser().getIdUser().equals(testUser.getIdUser()));
    }

    @Test
    @DisplayName("Should maintain referential integrity with usuario")
    void shouldMaintainReferentialIntegrityWithUser() {
        AuditRecord saved = auditRecordRepository.save(testRecord);

        Optional<AuditRecord> retrieved = auditRecordRepository.findByIdWithUser(saved.getIdAuditRecord());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(r -> {
                    assertThat(r.getFkIdUser()).isNotNull();
                    assertThat(r.getFkIdUser().getName()).isEqualTo("Admin User");
                });
    }

    @Test
    @DisplayName("Should update registro auditoria")
    void shouldUpdateRecord() {
        AuditRecord saved = auditRecordRepository.save(testRecord);

        saved.setOperationDetail("DELETE");
        auditRecordRepository.save(saved);

        Optional<AuditRecord> updated = auditRecordRepository.findById(saved.getIdAuditRecord());

        assertThat(updated).isPresent()
                .hasValueSatisfying(r -> assertThat(r.getOperationDetail()).isEqualTo("DELETE"));
    }

    @Test
    @DisplayName("Should delete registro auditoria")
    void shouldDeleteRecord() {
        AuditRecord saved = auditRecordRepository.save(testRecord);

        auditRecordRepository.deleteById(saved.getIdAuditRecord());

        Optional<AuditRecord> deleted = auditRecordRepository.findById(saved.getIdAuditRecord());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should find all registros")
    void shouldFindAll() {
        auditRecordRepository.save(testRecord);

        List<AuditRecord> all = auditRecordRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(r -> r.getIdAuditRecord().equals(testRecord.getIdAuditRecord()));
    }
}
