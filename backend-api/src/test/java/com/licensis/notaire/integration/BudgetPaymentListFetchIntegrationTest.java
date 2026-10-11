package com.licensis.notaire.integration;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #595: loading a budget must not pull its whole payment collection.
 * The collection is only read when a caller asks for it inside a persistence context.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Budget.paymentList is fetched lazily (issue #595)")
class BudgetPaymentListFetchIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("finding a budget does not load its payment collection")
    void findingABudgetDoesNotLoadItsPayments() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Integer id = em.createQuery("SELECT b.idBudget FROM Budget b ORDER BY b.idBudget", Integer.class)
                    .setMaxResults(1)
                    .getSingleResult();

            Budget budget = em.find(Budget.class, id);

            assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(budget, "paymentList")).isFalse();
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("listing budgets does not load any payment collection")
    void listingBudgetsDoesNotLoadPayments() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<Budget> budgets = em.createQuery("SELECT b FROM Budget b", Budget.class).getResultList();

            assertThat(budgets).isNotEmpty();
            assertThat(budgets)
                    .noneMatch(b -> entityManagerFactory.getPersistenceUnitUtil().isLoaded(b, "paymentList"));
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("the payment collection is still readable inside a persistence context")
    void paymentsAreStillReadableOnDemand() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Budget budget = em.createQuery("SELECT b FROM Budget b ORDER BY b.idBudget", Budget.class)
                    .setMaxResults(1)
                    .getSingleResult();
            long stored = em.createQuery("SELECT COUNT(p) FROM Payment p WHERE p.fkIdBudget = :b", Long.class)
                    .setParameter("b", budget)
                    .getSingleResult();

            Set<Payment> payments = budget.getPaymentList();

            assertThat(payments).hasSize((int) stored);
        } finally {
            em.close();
        }
    }
}
