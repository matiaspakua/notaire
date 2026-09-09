package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.BudgetTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ConceptoJpaController unit tests")
@ExtendWith(MockitoExtension.class)
class ConceptJpaControllerTest {

    @Mock
    private UserTransaction utx;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction tx;

    @Mock
    private Query query;

    private ConceptJpaController controller;

    @BeforeEach
    void setUp() {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = new ConceptJpaController(utx, emf);
    }

    @Nested
    @DisplayName("getEntityManager")
    class GetEntityManager {

        @Test
        @DisplayName("should create entity manager from factory")
        void shouldCreateEntityManager() {
            EntityManager result = controller.getEntityManager();
            assertThat(result).isSameAs(em);
            verify(emf).createEntityManager();
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @BeforeEach
        void setUpCreate() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should persist concepto and return its id")
        void shouldPersistAndReturnId() {
            Concept concept = new Concept(null, "Test Concept", 100.0f, 10);

            Integer result = controller.create(concept);

            assertThat(result).isNull(); // idConcepto is null before persist
            verify(tx).begin();
            verify(tx).commit();
            verify(em).persist(concept);
            verify(em).close();
        }

        @Test
        @DisplayName("should initialize plantillaPresupuestoList when null")
        void shouldInitializeTemplateListWhenNull() {
            Concept concept = new Concept(1, "Test", 100f, 10);
            concept.setBudgetTemplateList(null);

            controller.create(concept);

            assertThat(concept.getBudgetTemplateList()).isNotNull();
            assertThat(concept.getBudgetTemplateList()).isEmpty();
        }

        @Test
        @DisplayName("should close entity manager in finally block")
        void shouldCloseEmInFinally() {
            Concept concept = new Concept(1, "Test", 100f, 10);
            controller.create(concept);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("edit")
    class Edit {

        @BeforeEach
        void setUpEdit() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should merge concepto when version matches")
        void shouldMergeWhenVersionMatches() throws Exception {
            Integer id = 1;
            Concept concept = new Concept(id, "Updated", 200f, 20);
            concept.setVersion(0);

            Concept persistentConcept = new Concept(id, "Original", 100f, 10);
            persistentConcept.setVersion(0);
            persistentConcept.setBudgetTemplateList(new ArrayList<>());

            when(em.find(Concept.class, id)).thenReturn(persistentConcept);

            Boolean result = controller.edit(concept);

            assertThat(result).isTrue();
            verify(em).merge(concept);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassModifiedException when version differs")
        void shouldThrowWhenVersionDiffers() {
            Integer id = 1;
            Concept concept = new Concept(id, "Updated", 200f, 20);
            concept.setVersion(0);

            Concept persistentConcept = new Concept(id, "Original", 100f, 10);
            persistentConcept.setVersion(1); // DB has version 1, memory has 0

            when(em.find(Concept.class, id)).thenReturn(persistentConcept);

            assertThatThrownBy(() -> controller.edit(concept))
                    .isInstanceOf(ClassModifiedException.class);

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found and not close EM")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;
            Concept concept = new Concept(id, "Ghost", 100f, 10);

            when(em.find(Concept.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.edit(concept))
                    .isInstanceOf(ClassEliminatedException.class);

            // Note: edit() does NOT close EM in this path (it's a bug in the source)
            verify(em, never()).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when removing referenced PlantillaPresupuesto")
        void shouldThrowWhenRemovingReferencedTemplate() {
            Integer id = 1;
            BudgetTemplate pp = new BudgetTemplate();
            List<BudgetTemplate> oldList = new ArrayList<>();
            oldList.add(pp);

            Concept concept = new Concept(id, "Updated", 200f, 20);
            concept.setVersion(0);
            concept.setBudgetTemplateList(new ArrayList<>()); // empty new list

            Concept persistentConcept = new Concept(id, "Original", 100f, 10);
            persistentConcept.setVersion(0);
            persistentConcept.setBudgetTemplateList(oldList);

            when(em.find(Concept.class, id)).thenReturn(persistentConcept);

            assertThatThrownBy(() -> controller.edit(concept))
                    .isInstanceOf(IllegalOrphanException.class)
                    .hasMessageContaining("PlantillaPresupuesto");

            // Note: edit() does NOT close EM when throwing IllegalOrphanException from the merge path
            verify(em, never()).close();
            verify(tx, never()).commit();
        }
    }

    @Nested
    @DisplayName("destroy")
    class Destroy {

        @BeforeEach
        void setUpDestroy() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should remove concepto when no orphans exist")
        void shouldRemoveWhenNoOrphans() throws Exception {
            Integer id = 1;
            Concept concept = new Concept(id, "To delete", 100f, 10);
            concept.setBudgetTemplateList(new ArrayList<>());

            when(em.find(Concept.class, id)).thenReturn(concept);
            when(em.getReference(Concept.class, id)).thenReturn(concept);

            Boolean result = controller.destroy(id);

            assertThat(result).isTrue();
            verify(em).remove(concept);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;

            when(em.find(Concept.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(ClassEliminatedException.class);

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when PlantillaPresupuesto references exist")
        void shouldThrowWhenOrphansExist() {
            Integer id = 1;
            BudgetTemplate pp = new BudgetTemplate();
            List<BudgetTemplate> orphans = new ArrayList<>();
            orphans.add(pp);

            Concept concept = new Concept(id, "Has orphans", 100f, 10);
            concept.setBudgetTemplateList(orphans);

            when(em.find(Concept.class, id)).thenReturn(concept);
            when(em.getReference(Concept.class, id)).thenReturn(concept);

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(IllegalOrphanException.class)
                    .hasMessageContaining("cannot be destroyed");

            verify(em, never()).remove(any());
            verify(tx, never()).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on EntityNotFoundException")
        void shouldThrowOnEntityNotFoundDuringRemove() {
            Integer id = 1;
            Concept concept = new Concept(id, "Gone", 100f, 10);
            concept.setBudgetTemplateList(new ArrayList<>());

            when(em.find(Concept.class, id)).thenReturn(concept);
            when(em.getReference(Concept.class, id)).thenThrow(new EntityNotFoundException("Not found"));

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(NonexistentEntityException.class)
                    .hasMessageContaining("no longer exists");

            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findConceptoEntities")
    class FindConceptEntities {

        @Test
        @DisplayName("should return all entities")
        void shouldFindAll() {
            List<Concept> expectedList = new ArrayList<>();
            expectedList.add(new Concept(1, "A", 100f, 10));
            expectedList.add(new Concept(2, "B", 200f, 20));

            when(em.createQuery("select object(o) from Concepto as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concept> result = controller.findConceptEntities();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getName()).isEqualTo("A");
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated entities")
        void shouldFindPaginated() {
            List<Concept> expectedList = new ArrayList<>();
            expectedList.add(new Concept(1, "A", 100f, 10));

            when(em.createQuery("select object(o) from Concepto as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concept> result = controller.findConceptEntities(10, 0);

            assertThat(result).hasSize(1);
            verify(query).setMaxResults(10);
            verify(query).setFirstResult(0);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findConceptoByNombre")
    class FindConceptByName {

        @Test
        @DisplayName("should return conceptos matching name")
        void shouldFindByName() {
            String name = "Test Concept";
            List<Concept> expectedList = new ArrayList<>();
            expectedList.add(new Concept(1, name, 100f, 10));

            when(em.createNamedQuery("Concepto.findByNombre")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concept> result = controller.findConceptByName(name);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo(name);
            verify(query).setParameter("nombre", name);
            // Note: source code does NOT close EM in this method (pre-existing leak)
            verify(em, never()).close();
        }
    }

    @Nested
    @DisplayName("findConcepto")
    class FindConcept {

        @Test
        @DisplayName("should return concepto by id")
        void shouldFindById() {
            Concept expected = new Concept(1, "Found", 100f, 10);
            when(em.find(Concept.class, 1)).thenReturn(expected);

            Concept result = controller.findConcept(1);

            assertThat(result).isSameAs(expected);
            assertThat(result.getName()).isEqualTo("Found");
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(em.find(Concept.class, 999)).thenReturn(null);

            Concept result = controller.findConcept(999);

            assertThat(result).isNull();
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getConceptoCount")
    class GetConceptCount {

        @Test
        @DisplayName("should return count of conceptos")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from Concepto as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(42L);

            int result = controller.getConceptCount();

            assertThat(result).isEqualTo(42);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getNombreJpa")
    class GetNameJpa {

        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            String name = controller.getNameJpa();
            assertThat(name).isEqualTo(ConceptJpaController.class.getName());
        }
    }
}
