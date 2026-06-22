package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
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
class ConceptoJpaControllerTest {

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

    private ConceptoJpaController controller;

    @BeforeEach
    void setUp() {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = new ConceptoJpaController(utx, emf);
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
            Concepto concepto = new Concepto(null, "Test Concept", 100.0f, 10);

            Integer result = controller.create(concepto);

            assertThat(result).isNull(); // idConcepto is null before persist
            verify(tx).begin();
            verify(tx).commit();
            verify(em).persist(concepto);
            verify(em).close();
        }

        @Test
        @DisplayName("should initialize plantillaPresupuestoList when null")
        void shouldInitializePlantillaListWhenNull() {
            Concepto concepto = new Concepto(1, "Test", 100f, 10);
            concepto.setPlantillaPresupuestoList(null);

            controller.create(concepto);

            assertThat(concepto.getPlantillaPresupuestoList()).isNotNull();
            assertThat(concepto.getPlantillaPresupuestoList()).isEmpty();
        }

        @Test
        @DisplayName("should close entity manager in finally block")
        void shouldCloseEmInFinally() {
            Concepto concepto = new Concepto(1, "Test", 100f, 10);
            controller.create(concepto);
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
            Concepto concepto = new Concepto(id, "Updated", 200f, 20);
            concepto.setVersion(0);

            Concepto persistentConcepto = new Concepto(id, "Original", 100f, 10);
            persistentConcepto.setVersion(0);
            persistentConcepto.setPlantillaPresupuestoList(new ArrayList<>());

            when(em.find(Concepto.class, id)).thenReturn(persistentConcepto);

            Boolean result = controller.edit(concepto);

            assertThat(result).isTrue();
            verify(em).merge(concepto);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassModifiedException when version differs")
        void shouldThrowWhenVersionDiffers() {
            Integer id = 1;
            Concepto concepto = new Concepto(id, "Updated", 200f, 20);
            concepto.setVersion(0);

            Concepto persistentConcepto = new Concepto(id, "Original", 100f, 10);
            persistentConcepto.setVersion(1); // DB has version 1, memory has 0

            when(em.find(Concepto.class, id)).thenReturn(persistentConcepto);

            assertThatThrownBy(() -> controller.edit(concepto))
                    .isInstanceOf(ClassModifiedException.class);

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found and not close EM")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;
            Concepto concepto = new Concepto(id, "Ghost", 100f, 10);

            when(em.find(Concepto.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.edit(concepto))
                    .isInstanceOf(ClassEliminatedException.class);

            // Note: edit() does NOT close EM in this path (it's a bug in the source)
            verify(em, never()).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when removing referenced PlantillaPresupuesto")
        void shouldThrowWhenRemovingReferencedPlantilla() {
            Integer id = 1;
            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            List<PlantillaPresupuesto> oldList = new ArrayList<>();
            oldList.add(pp);

            Concepto concepto = new Concepto(id, "Updated", 200f, 20);
            concepto.setVersion(0);
            concepto.setPlantillaPresupuestoList(new ArrayList<>()); // empty new list

            Concepto persistentConcepto = new Concepto(id, "Original", 100f, 10);
            persistentConcepto.setVersion(0);
            persistentConcepto.setPlantillaPresupuestoList(oldList);

            when(em.find(Concepto.class, id)).thenReturn(persistentConcepto);

            assertThatThrownBy(() -> controller.edit(concepto))
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
            Concepto concepto = new Concepto(id, "To delete", 100f, 10);
            concepto.setPlantillaPresupuestoList(new ArrayList<>());

            when(em.find(Concepto.class, id)).thenReturn(concepto);
            when(em.getReference(Concepto.class, id)).thenReturn(concepto);

            Boolean result = controller.destroy(id);

            assertThat(result).isTrue();
            verify(em).remove(concepto);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;

            when(em.find(Concepto.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(ClassEliminatedException.class);

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when PlantillaPresupuesto references exist")
        void shouldThrowWhenOrphansExist() {
            Integer id = 1;
            PlantillaPresupuesto pp = new PlantillaPresupuesto();
            List<PlantillaPresupuesto> orphans = new ArrayList<>();
            orphans.add(pp);

            Concepto concepto = new Concepto(id, "Has orphans", 100f, 10);
            concepto.setPlantillaPresupuestoList(orphans);

            when(em.find(Concepto.class, id)).thenReturn(concepto);
            when(em.getReference(Concepto.class, id)).thenReturn(concepto);

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
            Concepto concepto = new Concepto(id, "Gone", 100f, 10);
            concepto.setPlantillaPresupuestoList(new ArrayList<>());

            when(em.find(Concepto.class, id)).thenReturn(concepto);
            when(em.getReference(Concepto.class, id)).thenThrow(new EntityNotFoundException("Not found"));

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(NonexistentEntityException.class)
                    .hasMessageContaining("no longer exists");

            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findConceptoEntities")
    class FindConceptoEntities {

        @Test
        @DisplayName("should return all entities")
        void shouldFindAll() {
            List<Concepto> expectedList = new ArrayList<>();
            expectedList.add(new Concepto(1, "A", 100f, 10));
            expectedList.add(new Concepto(2, "B", 200f, 20));

            when(em.createQuery("select object(o) from Concepto as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concepto> result = controller.findConceptoEntities();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getNombre()).isEqualTo("A");
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated entities")
        void shouldFindPaginated() {
            List<Concepto> expectedList = new ArrayList<>();
            expectedList.add(new Concepto(1, "A", 100f, 10));

            when(em.createQuery("select object(o) from Concepto as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concepto> result = controller.findConceptoEntities(10, 0);

            assertThat(result).hasSize(1);
            verify(query).setMaxResults(10);
            verify(query).setFirstResult(0);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findConceptoByNombre")
    class FindConceptoByNombre {

        @Test
        @DisplayName("should return conceptos matching nombre")
        void shouldFindByNombre() {
            String nombre = "Test Concept";
            List<Concepto> expectedList = new ArrayList<>();
            expectedList.add(new Concepto(1, nombre, 100f, 10));

            when(em.createNamedQuery("Concepto.findByNombre")).thenReturn(query);
            when(query.getResultList()).thenReturn(expectedList);

            List<Concepto> result = controller.findConceptoByNombre(nombre);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNombre()).isEqualTo(nombre);
            verify(query).setParameter("nombre", nombre);
            // Note: source code does NOT close EM in this method (pre-existing leak)
            verify(em, never()).close();
        }
    }

    @Nested
    @DisplayName("findConcepto")
    class FindConcepto {

        @Test
        @DisplayName("should return concepto by id")
        void shouldFindById() {
            Concepto expected = new Concepto(1, "Found", 100f, 10);
            when(em.find(Concepto.class, 1)).thenReturn(expected);

            Concepto result = controller.findConcepto(1);

            assertThat(result).isSameAs(expected);
            assertThat(result.getNombre()).isEqualTo("Found");
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(em.find(Concepto.class, 999)).thenReturn(null);

            Concepto result = controller.findConcepto(999);

            assertThat(result).isNull();
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getConceptoCount")
    class GetConceptoCount {

        @Test
        @DisplayName("should return count of conceptos")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from Concepto as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(42L);

            int result = controller.getConceptoCount();

            assertThat(result).isEqualTo(42);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getNombreJpa")
    class GetNombreJpa {

        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            String nombre = controller.getNombreJpa();
            assertThat(nombre).isEqualTo(ConceptoJpaController.class.getName());
        }
    }
}
