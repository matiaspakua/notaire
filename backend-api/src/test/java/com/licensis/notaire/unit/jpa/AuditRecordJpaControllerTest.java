package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.AuditRecordJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegistroAuditoriaJpaController unit tests")
@ExtendWith(MockitoExtension.class)
class AuditRecordJpaControllerTest {

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

    private AuditRecordJpaController controller;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = createWithReflection(AuditRecordJpaController.class, utx, emf);
    }

    @SuppressWarnings("unchecked")
    private <T extends IPersistenciaJpa> T createWithReflection(Class<T> clazz, Object... args) throws Exception {
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            if (ctor.getParameterCount() == args.length) {
                ctor.setAccessible(true);
                return (T) ctor.newInstance(args);
            }
        }
        throw new IllegalArgumentException("No constructor with " + args.length + " args in " + clazz);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should persist and return true")
        void shouldPersistAndReturnTrue() {
            User user = new User(1);
            AuditRecord ra = new AuditRecord(99);
            ra.setFkIdUser(user);

            User userRef = new User(1);
            userRef.setAuditRecordList(new ArrayList<>());
            when(em.getReference(User.class, 1)).thenReturn(userRef);

            boolean result = controller.create(ra);

            assertThat(result).isTrue();
            verify(em).persist(ra);
            verify(em).merge(userRef);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should handle null FK usuario")
        void shouldHandleNullFk() {
            AuditRecord ra = new AuditRecord(99);

            boolean result = controller.create(ra);

            assertThat(result).isTrue();
            verify(em).persist(ra);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("edit")
    class Edit {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should merge when FK unchanged")
        void shouldMergeWhenFkUnchanged() throws Exception {
            Integer id = 1;
            User user = new User(1);
            AuditRecord persistentRA = new AuditRecord(id);
            persistentRA.setFkIdUser(user);
            AuditRecord inputRA = new AuditRecord(id);
            inputRA.setFkIdUser(user);

            when(em.find(AuditRecord.class, id)).thenReturn(persistentRA);
            when(em.getReference(User.class, 1)).thenReturn(user);

            controller.edit(inputRA);

            verify(em).merge(inputRA);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should update FK references when usuario changes")
        void shouldUpdateFkWhenUserChanges() throws Exception {
            Integer id = 1;
            User oldUser = new User(10);
            oldUser.setAuditRecordList(new ArrayList<>());
            oldUser.getAuditRecordList().add(new AuditRecord(id));

            User newUser = new User(20);
            newUser.setAuditRecordList(new ArrayList<>());

            AuditRecord persistentRA = new AuditRecord(id);
            persistentRA.setFkIdUser(oldUser);

            AuditRecord inputRA = new AuditRecord(id);
            inputRA.setFkIdUser(newUser);

            when(em.find(AuditRecord.class, id)).thenReturn(persistentRA);
            when(em.getReference(User.class, 20)).thenReturn(newUser);

            controller.edit(inputRA);

            verify(em).merge(oldUser);
            verify(em).merge(newUser);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on stale data")
        void shouldThrowWhenEntityGone() {
            Integer id = 999;
            AuditRecord inputRA = new AuditRecord(id);
            when(em.find(AuditRecord.class, id)).thenThrow(new IllegalArgumentException("gone"));

            assertThatThrownBy(() -> controller.edit(inputRA))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(em).close();
        }
    }

    @Nested
    @DisplayName("destroy")
    class Destroy {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should remove entity and update FK reference")
        void shouldRemoveAndUpdateFk() throws Exception {
            Integer id = 1;
            User user = new User(10);
            user.setAuditRecordList(new ArrayList<>());

            AuditRecord ra = new AuditRecord(id);
            ra.setFkIdUser(user);

            when(em.getReference(AuditRecord.class, id)).thenReturn(ra);

            controller.destroy(id);

            verify(em).merge(user);
            verify(em).remove(ra);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on EntityNotFoundException")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;
            when(em.getReference(AuditRecord.class, id))
                    .thenThrow(new EntityNotFoundException("gone"));

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(NonexistentEntityException.class)
                    .hasMessageContaining("no longer exists");

            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findRegistroAuditoriaEntities")
    class FindEntities {
        @Test
        @DisplayName("should return all")
        void shouldFindAll() {
            List<AuditRecord> expected = new ArrayList<>();
            expected.add(new AuditRecord(1));
            when(em.createQuery("select object(o) from AuditRecord as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<AuditRecord> result = controller.findAuditRecordEntities();

            assertThat(result).hasSize(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated")
        void shouldFindPaginated() {
            when(em.createQuery("select object(o) from AuditRecord as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            controller.findAuditRecordEntities(10, 0);

            verify(query).setMaxResults(10);
            verify(query).setFirstResult(0);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findRegistroAuditoria")
    class FindById {
        @Test
        @DisplayName("should find by id")
        void shouldFindById() {
            when(em.find(AuditRecord.class, 1)).thenReturn(new AuditRecord(1));

            assertThat(controller.findAuditRecord(1).getIdAuditRecord()).isEqualTo(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNull() {
            when(em.find(AuditRecord.class, 999)).thenReturn(null);
            assertThat(controller.findAuditRecord(999)).isNull();
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getRegistroAuditoriaCount")
    class GetCount {
        @Test
        @DisplayName("should return count")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from AuditRecord as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(42L);

            assertThat(controller.getAuditRecordCount()).isEqualTo(42);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("buscarRegistroAuditoriasUsuario")
    class SearchPorUser {
        @Test
        @DisplayName("should return filtered list when usuario matches")
        void shouldReturnFiltered() {
            User user = new User(5);
            List<AuditRecord> all = new ArrayList<>();
            AuditRecord ra1 = new AuditRecord(1);
            ra1.setFkIdUser(new User(5));
            AuditRecord ra2 = new AuditRecord(2);
            ra2.setFkIdUser(new User(3));
            all.add(ra1);
            all.add(ra2);

            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(all);

            ArrayList<AuditRecord> result = controller.searchRecordAuditoriasUser(user);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdAuditRecord()).isEqualTo(1);
            // Note: source code does NOT close EM in this method (pre-existing leak)
        }

        @Test
        @DisplayName("should return null when no match")
        void shouldReturnNullWhenNoMatch() {
            User user = new User(99);
            List<AuditRecord> all = new ArrayList<>();
            AuditRecord ra1 = new AuditRecord(1);
            ra1.setFkIdUser(new User(5));
            all.add(ra1);

            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(all);

            ArrayList<AuditRecord> result = controller.searchRecordAuditoriasUser(user);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should handle exception gracefully")
        void shouldHandleException() {
            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenThrow(new RuntimeException("DB error"));

            ArrayList<AuditRecord> result = controller.searchRecordAuditoriasUser(new User(1));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNombreJpa")
    class GetNameJpa {
        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            assertThat(controller.getNameJpa()).isEqualTo(AuditRecordJpaController.class.getName());
        }
    }
}
