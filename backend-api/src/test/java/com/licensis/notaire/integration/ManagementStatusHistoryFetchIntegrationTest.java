package com.licensis.notaire.integration;

import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #595: loading a management status (directly, or through every history row and
 * management that references it) must not pull its whole history collection. The
 * collection is only read when a caller asks for it inside a transaction.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("ManagementStatus.historyList is fetched lazily (issue #595)")
class ManagementStatusHistoryFetchIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("finding a status does not load its history collection")
    void findingAStatusDoesNotLoadItsHistory() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Integer id = em.createQuery("SELECT s.idManagementStatus FROM ManagementStatus s ORDER BY s.idManagementStatus",
                    Integer.class).setMaxResults(1).getSingleResult();

            ManagementStatus status = em.find(ManagementStatus.class, id);

            assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(status, "historyList")).isFalse();
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("listing statuses does not load any history collection")
    void listingStatusesDoesNotLoadHistory() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<ManagementStatus> statuses = em.createQuery("SELECT s FROM ManagementStatus s", ManagementStatus.class)
                    .getResultList();

            assertThat(statuses).isNotEmpty();
            assertThat(statuses).noneMatch(s -> entityManagerFactory.getPersistenceUnitUtil().isLoaded(s, "historyList"));
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("the history collection is still readable inside a persistence context")
    void historyIsStillReadableOnDemand() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            ManagementStatus status = em.createQuery("SELECT s FROM ManagementStatus s ORDER BY s.idManagementStatus",
                    ManagementStatus.class).setMaxResults(1).getSingleResult();
            long stored = em.createQuery("SELECT COUNT(h) FROM History h WHERE h.fkIdManagementStatus = :s", Long.class)
                    .setParameter("s", status).getSingleResult();

            java.util.Set<History> history = status.getHistoryList();

            assertThat(history).hasSize((int) stored);
        } finally {
            em.close();
        }
    }
}
