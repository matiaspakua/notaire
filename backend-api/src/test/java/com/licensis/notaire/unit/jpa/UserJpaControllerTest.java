package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.UserJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UsuarioJpaController unit tests")
@ExtendWith(MockitoExtension.class)
class UserJpaControllerTest {

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

    private UserJpaController controller;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = createWithReflection(UserJpaController.class, utx, emf);
    }

    /**
     * Helper to instantiate a JpaController that has a private constructor.
     */
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

    // -- create -----------------------------------------------------------

    @Nested
    @DisplayName("create")
    class Create {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should persist usuario and manage FK references")
        void shouldPersistAndManageFK() {
            Person person = new Person(10);
            List<AuditRecord> raList = new ArrayList<>();
            AuditRecord ra = new AuditRecord(99);
            raList.add(ra);

            User user = new User(1, "admin", "pass", true, "admin");
            user.setFkIdPerson(person);
            user.setAuditRecordList(raList);

            // Mock FK references for getReference
            Person personRef = new Person(10);
            personRef.setUserList(new ArrayList<>());
            AuditRecord raRef = new AuditRecord(99);
            raRef.setFkIdUser(null);

            when(em.getReference(Person.class, 10)).thenReturn(personRef);
            when(em.getReference(AuditRecord.class, 99)).thenReturn(raRef);

            controller.create(user);

            verify(em).persist(user);
            verify(em).merge(personRef);
            verify(em).merge(raRef);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should initialize empty audit list when null")
        void shouldInitEmptyAuditList() {
            User user = new User(1, "u", "p", true, "user");
            user.setAuditRecordList(null);

            controller.create(user);

            assertThat(user.getAuditRecordList()).isNotNull().isEmpty();
            verify(em).persist(user);
            verify(em).close();
        }

        @Test
        @DisplayName("should handle null FK person")
        void shouldHandleNullFkPerson() {
            User user = new User(1, "u", "p", true, "user");
            user.setAuditRecordList(new ArrayList<>());
            user.setFkIdPerson(null);

            controller.create(user);

            verify(em).persist(user);
            verify(em).close();
        }
    }

    // -- edit -------------------------------------------------------------

    @Nested
    @DisplayName("edit")
    class Edit {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should merge usuario with same FK and no orphan changes")
        void shouldMergeSuccessfully() throws Exception {
            Integer id = 1;
            Person person = new Person(10);
            person.setUserList(new ArrayList<>());

            User persistentUser = new User(id, "old", "oldp", true, "user");
            persistentUser.setFkIdPerson(person);
            persistentUser.setAuditRecordList(new ArrayList<>());

            User inputUser = new User(id, "new", "newp", true, "admin");
            inputUser.setFkIdPerson(person);
            inputUser.setAuditRecordList(new ArrayList<>());

            when(em.find(User.class, id)).thenReturn(persistentUser);

            controller.edit(inputUser);

            verify(em).merge(inputUser);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when removing audit entries")
        void shouldThrowOnOrphan() {
            Integer id = 1;
            AuditRecord ra = new AuditRecord(99);
            List<AuditRecord> oldList = new ArrayList<>();
            oldList.add(ra);

            User persistentUser = new User(id, "u", "p", true, "user");
            persistentUser.setAuditRecordList(oldList);
            persistentUser.setFkIdPerson(null);

            User inputUser = new User(id, "u", "p", true, "user");
            inputUser.setAuditRecordList(new ArrayList<>());
            inputUser.setFkIdPerson(null);

            when(em.find(User.class, id)).thenReturn(persistentUser);

            assertThatThrownBy(() -> controller.edit(inputUser))
                    .isInstanceOf(IllegalOrphanException.class)
                    .hasMessageContaining("RegistroAuditoria");

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should attach new FK person reference")
        void shouldAttachNewFkPerson() throws Exception {
            Integer id = 1;
            Person oldPerson = new Person(10);
            oldPerson.setUserList(new ArrayList<>());
            Person newPerson = new Person(20);
            newPerson.setUserList(new ArrayList<>());

            User persistentUser = new User(id, "u", "p", true, "user");
            persistentUser.setFkIdPerson(oldPerson);
            persistentUser.setAuditRecordList(new ArrayList<>());

            User inputUser = new User(id, "u", "p", true, "user");
            inputUser.setFkIdPerson(newPerson);
            inputUser.setAuditRecordList(new ArrayList<>());

            when(em.find(User.class, id)).thenReturn(persistentUser);
            when(em.getReference(Person.class, 20)).thenReturn(newPerson);

            controller.edit(inputUser);

            verify(em).merge(oldPerson);
            verify(em).merge(newPerson);
            verify(tx).commit();
            verify(em).close();
        }
    }

    // -- destroy ----------------------------------------------------------

    @Nested
    @DisplayName("destroy")
    class Destroy {
        @BeforeEach
        void setUpTx() {
            when(em.getTransaction()).thenReturn(tx);
        }

        @Test
        @DisplayName("should remove usuario when no orphans")
        void shouldRemoveSuccessfully() throws Exception {
            Integer id = 1;
            User user = new User(id, "u", "p", true, "user");
            user.setAuditRecordList(new ArrayList<>());
            user.setFkIdPerson(null);

            when(em.getReference(User.class, id)).thenReturn(user);

            controller.destroy(id);

            verify(em).remove(user);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when audit entries exist")
        void shouldThrowOnOrphan() {
            Integer id = 1;
            AuditRecord ra = new AuditRecord(99);
            List<AuditRecord> orphans = new ArrayList<>();
            orphans.add(ra);

            User user = new User(id, "u", "p", true, "user");
            user.setAuditRecordList(orphans);

            when(em.getReference(User.class, id)).thenReturn(user);

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(IllegalOrphanException.class)
                    .hasMessageContaining("cannot be destroyed");

            verify(em).close();
            verify(em, never()).remove(any());
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on EntityNotFoundException")
        void shouldThrowOnEntityNotFound() {
            Integer id = 999;
            when(em.getReference(User.class, id)).thenThrow(new EntityNotFoundException("gone"));

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(NonexistentEntityException.class)
                    .hasMessageContaining("no longer exists");

            verify(em).close();
        }

        @Test
        @DisplayName("should remove FK person reference on destroy")
        void shouldRemoveFkPersonRef() throws Exception {
            Integer id = 1;
            Person person = new Person(10);
            person.setUserList(new ArrayList<>());
            person.getUserList().add(new User(id));

            User user = new User(id, "u", "p", true, "user");
            user.setAuditRecordList(new ArrayList<>());
            user.setFkIdPerson(person);

            when(em.getReference(User.class, id)).thenReturn(user);

            controller.destroy(id);

            verify(em).merge(person);
            verify(em).remove(user);
            verify(tx).commit();
            verify(em).close();
        }
    }

    // -- find methods -----------------------------------------------------

    @Nested
    @DisplayName("findUsuariosEntities")
    class FindUsersEntities {
        @Test
        @DisplayName("should return all")
        void shouldFindAll() {
            List<User> expected = new ArrayList<>();
            expected.add(new User(1, "a", "p", true, "user"));
            when(em.createQuery("select object(o) from User as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<User> result = controller.findUsersEntities();

            assertThat(result).hasSize(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated")
        void shouldFindPaginated() {
            when(em.createQuery("select object(o) from User as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            controller.findUsersEntities(5, 2);

            verify(query).setMaxResults(5);
            verify(query).setFirstResult(2);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findUsuarios")
    class FindUsers {
        @Test
        @DisplayName("should find by id")
        void shouldFindById() {
            when(em.find(User.class, 1)).thenReturn(new User(1));

            User result = controller.findUsers(1);

            assertThat(result.getIdUser()).isEqualTo(1);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getUsuariosCount")
    class GetUsersCount {
        @Test
        @DisplayName("should return count")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from User as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(5L);

            int result = controller.getUsersCount();

            assertThat(result).isEqualTo(5);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("buscarUsuarios")
    class SearchUsers {
        @Test
        @DisplayName("should use named query")
        void shouldUseNamedQuery() {
            List<User> expected = new ArrayList<>();
            expected.add(new User(1));
            when(em.createNamedQuery("Usuario.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<User> result = controller.searchUsers();

            assertThat(result).hasSize(1);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findUsuarioByPersona")
    class FindUserByPerson {
        @Test
        @DisplayName("should return null when idPersona is null")
        void shouldReturnNullWhenNullArg() {
            User result = controller.findUserByPerson(null);
            assertThat(result).isNull();
            // No EM created at all
            verify(emf, never()).createEntityManager();
        }

        @Test
        @DisplayName("should return usuario when found")
        void shouldReturnFound() {
            User expected = new User(1);
            List<User> results = new ArrayList<>();
            results.add(expected);
            when(em.createNamedQuery("Usuario.findByFkIdPersona")).thenReturn(query);
            when(query.getResultList()).thenReturn(results);

            User result = controller.findUserByPerson(10);

            assertThat(result).isSameAs(expected);
            verify(query).setParameter("idPersona", 10);
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(em.createNamedQuery("Usuario.findByFkIdPersona")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            User result = controller.findUserByPerson(10);

            assertThat(result).isNull();
            verify(em).close();
        }
    }

    // -- modificarUsuario -------------------------------------------------

    @Nested
    @DisplayName("modificarUsuario")
    class ModificarUser {
        @Test
        @DisplayName("should update entity fields when version matches")
        void shouldUpdateWhenVersionMatches() throws Exception {
            Integer id = 1;
            User persistentUser = new User(id, "old", "oldp", true, "user");
            persistentUser.setVersion(0);

            User inputUser = new User(id, "new", "newp", false, "admin");
            inputUser.setVersion(0);

            when(em.find(User.class, id)).thenReturn(persistentUser);
            when(em.getTransaction()).thenReturn(tx);

            Boolean result = controller.modificarUser(inputUser);

            assertThat(result).isTrue();
            assertThat(persistentUser.getName()).isEqualTo("new");
            assertThat(persistentUser.getPassword()).isEqualTo("newp");
            assertThat(persistentUser.getStatus()).isFalse();
            assertThat(persistentUser.getType()).isEqualTo("admin");
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassModifiedException when version differs")
        void shouldThrowOnVersionMismatch() {
            Integer id = 1;
            User persistentUser = new User(id, "u", "p", true, "user");
            persistentUser.setVersion(5);

            User inputUser = new User(id, "u", "p", true, "user");
            inputUser.setVersion(0);

            when(em.find(User.class, id)).thenReturn(persistentUser);

            assertThatThrownBy(() -> controller.modificarUser(inputUser))
                    .isInstanceOf(ClassModifiedException.class);

            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found")
        void shouldThrowWhenNotFound() {
            Integer id = 999;
            User inputUser = new User(id, "u", "p", true, "user");

            when(em.find(User.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.modificarUser(inputUser))
                    .isInstanceOf(ClassEliminatedException.class);

            verify(em).close();
        }
    }

    // -- getNombreJpa -----------------------------------------------------

    @Nested
    @DisplayName("getNombreJpa")
    class GetNameJpa {
        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            assertThat(controller.getNameJpa()).isEqualTo(UserJpaController.class.getName());
        }
    }
}
