package com.licensis.notaire.integration;

import com.licensis.notaire.api.HistoryController;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the historial delete silently no-op-ing (CU13 audit trail).
 *
 * <p>The bug only reproduces when the row being deleted was created and committed in a
 * prior, separate transaction — exactly what happens across two real HTTP requests. A
 * single {@code @Transactional} test method that creates and deletes the row in one
 * persistence context does not exercise the failure: it must be split into two
 * transactions via {@link TestTransaction} to mirror production.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
class HistoryDeleteIntegrationTest {

    @Autowired
    private HistoryController historyController;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    @Test
    @Transactional
    @DisplayName("Should delete history row created in a prior, already-committed transaction")
    void shouldDeleteHistoryCreatedInPriorTransaction() {
        Integer id = createAndCommitHistory();

        ResponseEntity<Void> response = historyController.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(historyRepository.existsById(id)).isFalse();
    }

    private Integer createAndCommitHistory() {
        IdentificationType type = identificationTypeRepository.findById(1).orElseThrow();

        Person person = new Person();
        person.setFirstName("X");
        person.setLastName("Y");
        person.setIdentificationNumber("1");
        person.setFkIdIdentificationType(type);
        person = personRepository.save(person);

        ManagementStatus status = statusRepository.findById(1).orElseThrow();

        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setNumber(1);
        management.setEncabezado("test");
        management.setFkIdNotaryPerson(person);
        management.setFkIdManagementStatus(status);
        management = managementRepository.save(management);

        History history = new History();
        history.setDate(new Date());
        history.setNotes("obs");
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(status);
        history = historyRepository.save(history);

        Integer id = history.getIdHistory();

        // Commit and start a fresh transaction so the row above is genuinely persisted
        // and reloaded from scratch by the code under test, matching a real HTTP request
        // that arrives after the row was created by an earlier, unrelated request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return id;
    }
}
