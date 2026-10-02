package com.licensis.notaire.integration;


import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.repository.PaymentRepository;
import com.licensis.notaire.repository.BudgetRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PagoRepository Integration Tests")
class PaymentRepositoryIntegrationTest extends RepositoryIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private Payment testPayment;
    private Budget testBudget;
    private Person testPerson;

    @BeforeEach
    void setUp() {
        IdentificationType identificationType = new IdentificationType();
        identificationType.setName("DNI");
        identificationTypeRepository.save(identificationType);

        testPerson = new Person();
        testPerson.setFirstName("Cliente");
        testPerson.setLastName("Test");
        testPerson.setIdentificationNumber("12345678");
        testPerson.setIsClient(true);
        testPerson.setFkIdIdentificationType(identificationType);
        personRepository.save(testPerson);

        testBudget = new Budget();
        testBudget.setNumber((int) (System.currentTimeMillis() % 10000));
        testBudget.setDate(new Date());
        testBudget.setEncabezado("Presupuesto Test");
        testBudget.setStatus("PENDIENTE");
        testBudget.setPropertyAmount(new java.math.BigDecimal("500000"));
        testBudget.setFkIdPerson(testPerson);
        testBudget = budgetRepository.save(testBudget);

        testPayment = new Payment();
        testPayment.setAmount(new java.math.BigDecimal("100000"));
        testPayment.setDate(new Date());
        testPayment.setBudget(testBudget);
    }

    @Test
    @DisplayName("Should persist and retrieve pago")
    void shouldPersistAndRetrievePayment() {
        Payment saved = paymentRepository.save(testPayment);

        assertThat(saved.getIdPayment()).isNotNull();

        Optional<Payment> retrieved = paymentRepository.findById(saved.getIdPayment());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getAmount()).isEqualByComparingTo(new java.math.BigDecimal("100000"));
                    assertThat(p.getDate()).isNotNull();
                });
    }

    @Test
    @DisplayName("Should find payments by budget")
    void shouldFindByBudget() {
        paymentRepository.save(testPayment);

        List<Payment> found = paymentRepository.findByFkIdBudgetIdBudget(testBudget.getIdBudget());

        assertThat(found).hasSize(1)
                .allMatch(p -> p.getBudget().getIdBudget().equals(testBudget.getIdBudget()));
    }

    @Test
    @DisplayName("Should calculate sum of montos by budget")
    void shouldSumAmountByBudget() {
        paymentRepository.save(testPayment);

        Payment pago2 = new Payment();
        pago2.setAmount(new java.math.BigDecimal("50000"));
        pago2.setDate(new Date());
        pago2.setBudget(testBudget);
        paymentRepository.save(pago2);

        java.math.BigDecimal sum = paymentRepository.sumAmountByBudgetId(testBudget.getIdBudget());

        assertThat(sum).isEqualByComparingTo(new java.math.BigDecimal("150000"));
    }

    @Test
    @DisplayName("Should return null for sum when no payments exist")
    void shouldReturnNullForSumWhenNoPaymentsExist() {
        java.math.BigDecimal sum = paymentRepository.sumAmountByBudgetId(9999);

        assertThat(sum).isNull();
    }

    @Test
    @DisplayName("Should find payments by date range")
    void shouldFindByDateRange() {
        paymentRepository.save(testPayment);

        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);

        List<Payment> found = paymentRepository.findByDateBetween(startDate, endDate);

        assertThat(found).isNotEmpty()
                .anyMatch(p -> p.getIdPayment().equals(testPayment.getIdPayment()));
    }

    @Test
    @DisplayName("Should update pago")
    void shouldUpdatePayment() {
        Payment saved = paymentRepository.save(testPayment);

        saved.setAmount(new java.math.BigDecimal("120000"));
        saved.setNotes("Pago actualizado");
        paymentRepository.save(saved);

        Optional<Payment> updated = paymentRepository.findById(saved.getIdPayment());

        assertThat(updated).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getAmount()).isEqualByComparingTo(new java.math.BigDecimal("120000"));
                    assertThat(p.getNotes()).isEqualTo("Pago actualizado");
                });
    }

    @Test
    @DisplayName("Should delete pago")
    void shouldDeletePayment() {
        Payment saved = paymentRepository.save(testPayment);

        paymentRepository.deleteById(saved.getIdPayment());

        Optional<Payment> deleted = paymentRepository.findById(saved.getIdPayment());

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should maintain referential integrity with budget")
    void shouldMaintainReferentialIntegrityWithBudget() {
        Payment saved = paymentRepository.save(testPayment);

        Optional<Payment> retrieved = paymentRepository.findById(saved.getIdPayment());

        assertThat(retrieved).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getBudget()).isNotNull();
                    assertThat(p.getBudget().getIdBudget()).isEqualTo(testBudget.getIdBudget());
                });
    }

    @Test
    @DisplayName("Should find all payments")
    void shouldFindAll() {
        paymentRepository.save(testPayment);

        List<Payment> all = paymentRepository.findAll();

        assertThat(all).isNotEmpty()
                .anyMatch(p -> p.getIdPayment().equals(testPayment.getIdPayment()));
    }
}
