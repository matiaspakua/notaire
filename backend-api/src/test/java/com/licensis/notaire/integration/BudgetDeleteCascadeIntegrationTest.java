package com.licensis.notaire.integration;

import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@code Presupuesto}'s
 * {@code @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)} collection of
 * {@link Pago} — the second highest-risk cascade shape identified in design.md "Riesgos"
 * for reverting a delete via Hibernate re-managing the loaded, cascaded children in the
 * same persistence context (#957).
 *
 * <p>Follows the same two-transaction shape as {@link HistorialDeleteIntegrationTest}:
 * the parent and child are created and committed in a prior transaction, matching a real
 * HTTP delete request that arrives after both rows already exist.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
class BudgetDeleteCascadeIntegrationTest {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    @Test
    @Transactional
    @DisplayName("Should delete budget and cascade-delete its pago children")
    void shouldDeleteBudgetWithCascadedChildren() {
        Integer idBudget = createAndCommitBudgetWithPayment();

        budgetRepository.deleteById(idBudget);

        assertThat(budgetRepository.existsById(idBudget)).isFalse();
        assertThat(paymentRepository.findByFkIdBudgetIdBudget(idBudget)).isEmpty();
    }

    private Integer createAndCommitBudgetWithPayment() {
        IdentificationType type = identificationTypeRepository.findById(1).orElseThrow();

        Person person = new Person();
        person.setFirstName("X");
        person.setLastName("Y");
        person.setIdentificationNumber("cascade-test-1");
        person.setFkIdIdentificationType(type);
        person = personRepository.save(person);

        Budget budget = new Budget();
        budget.setDate(new Date());
        budget.setNumber(1);
        budget.setStatus("ACTIVO");
        budget.setEncabezado("Cascade Test Presupuesto");
        budget.setFkIdPerson(person);
        budget = budgetRepository.save(budget);

        Payment payment = new Payment();
        payment.setAmount(100f);
        payment.setDate(new Date());
        payment.setBudget(budget);
        paymentRepository.save(payment);

        Integer idBudget = budget.getIdBudget();

        // Commit and start a fresh transaction so both rows above are genuinely
        // persisted and reloaded from scratch by the code under test, matching a real
        // HTTP delete request that arrives after they were created by an earlier request.
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        return idBudget;
    }
}
