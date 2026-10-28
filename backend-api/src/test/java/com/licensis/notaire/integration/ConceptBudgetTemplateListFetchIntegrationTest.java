package com.licensis.notaire.integration;

import com.licensis.notaire.business.Concept;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Concept.budgetTemplateList is fetched lazily (issue #595)")
class ConceptBudgetTemplateListFetchIntegrationTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("finding a concept does not load its budget-template collection")
    void findingAConceptDoesNotLoadTemplates() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Integer id = em.createQuery("SELECT c.idConcept FROM Concept c ORDER BY c.idConcept", Integer.class)
                    .setMaxResults(1)
                    .getSingleResult();

            Concept concept = em.find(Concept.class, id);

            assertThat(entityManagerFactory.getPersistenceUnitUtil().isLoaded(concept, "budgetTemplateList"))
                    .isFalse();
        } finally {
            em.close();
        }
    }

    @Test
    @DisplayName("listing concepts does not load any budget-template collection")
    void listingConceptsDoesNotLoadTemplates() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            List<Concept> concepts = em.createQuery("SELECT c FROM Concept c", Concept.class).getResultList();

            assertThat(concepts).isNotEmpty();
            assertThat(concepts)
                    .noneMatch(c -> entityManagerFactory.getPersistenceUnitUtil().isLoaded(c, "budgetTemplateList"));
        } finally {
            em.close();
        }
    }
}
