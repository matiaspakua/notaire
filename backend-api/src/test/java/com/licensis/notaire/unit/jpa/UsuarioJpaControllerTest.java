package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
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
class UsuarioJpaControllerTest {

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

    private UsuarioJpaController controller;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = createWithReflection(UsuarioJpaController.class, utx, emf);
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
            Persona persona = new Persona(10);
            List<RegistroAuditoria> raList = new ArrayList<>();
            RegistroAuditoria ra = new RegistroAuditoria(99);
            raList.add(ra);

            Usuario usuario = new Usuario(1, "admin", "pass", true, "admin");
            usuario.setFkIdPersona(persona);
            usuario.setRegistroAuditoriaList(raList);

            // Mock FK references for getReference
            Persona personaRef = new Persona(10);
            personaRef.setUsuariosList(new ArrayList<>());
            RegistroAuditoria raRef = new RegistroAuditoria(99);
            raRef.setFkIdUsuario(null);

            when(em.getReference(Persona.class, 10)).thenReturn(personaRef);
            when(em.getReference(RegistroAuditoria.class, 99)).thenReturn(raRef);

            controller.create(usuario);

            verify(em).persist(usuario);
            verify(em).merge(personaRef);
            verify(em).merge(raRef);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should initialize empty audit list when null")
        void shouldInitEmptyAuditList() {
            Usuario usuario = new Usuario(1, "u", "p", true, "user");
            usuario.setRegistroAuditoriaList(null);

            controller.create(usuario);

            assertThat(usuario.getRegistroAuditoriaList()).isNotNull().isEmpty();
            verify(em).persist(usuario);
            verify(em).close();
        }

        @Test
        @DisplayName("should handle null FK persona")
        void shouldHandleNullFkPersona() {
            Usuario usuario = new Usuario(1, "u", "p", true, "user");
            usuario.setRegistroAuditoriaList(new ArrayList<>());
            usuario.setFkIdPersona(null);

            controller.create(usuario);

            verify(em).persist(usuario);
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
            Persona persona = new Persona(10);
            persona.setUsuariosList(new ArrayList<>());

            Usuario persistentUsuario = new Usuario(id, "old", "oldp", true, "user");
            persistentUsuario.setFkIdPersona(persona);
            persistentUsuario.setRegistroAuditoriaList(new ArrayList<>());

            Usuario inputUsuario = new Usuario(id, "new", "newp", true, "admin");
            inputUsuario.setFkIdPersona(persona);
            inputUsuario.setRegistroAuditoriaList(new ArrayList<>());

            when(em.find(Usuario.class, id)).thenReturn(persistentUsuario);

            controller.edit(inputUsuario);

            verify(em).merge(inputUsuario);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when removing audit entries")
        void shouldThrowOnOrphan() {
            Integer id = 1;
            RegistroAuditoria ra = new RegistroAuditoria(99);
            List<RegistroAuditoria> oldList = new ArrayList<>();
            oldList.add(ra);

            Usuario persistentUsuario = new Usuario(id, "u", "p", true, "user");
            persistentUsuario.setRegistroAuditoriaList(oldList);
            persistentUsuario.setFkIdPersona(null);

            Usuario inputUsuario = new Usuario(id, "u", "p", true, "user");
            inputUsuario.setRegistroAuditoriaList(new ArrayList<>());
            inputUsuario.setFkIdPersona(null);

            when(em.find(Usuario.class, id)).thenReturn(persistentUsuario);

            assertThatThrownBy(() -> controller.edit(inputUsuario))
                    .isInstanceOf(IllegalOrphanException.class)
                    .hasMessageContaining("RegistroAuditoria");

            verify(em).close();
            verify(tx, never()).commit();
        }

        @Test
        @DisplayName("should attach new FK persona reference")
        void shouldAttachNewFkPersona() throws Exception {
            Integer id = 1;
            Persona oldPersona = new Persona(10);
            oldPersona.setUsuariosList(new ArrayList<>());
            Persona newPersona = new Persona(20);
            newPersona.setUsuariosList(new ArrayList<>());

            Usuario persistentUsuario = new Usuario(id, "u", "p", true, "user");
            persistentUsuario.setFkIdPersona(oldPersona);
            persistentUsuario.setRegistroAuditoriaList(new ArrayList<>());

            Usuario inputUsuario = new Usuario(id, "u", "p", true, "user");
            inputUsuario.setFkIdPersona(newPersona);
            inputUsuario.setRegistroAuditoriaList(new ArrayList<>());

            when(em.find(Usuario.class, id)).thenReturn(persistentUsuario);
            when(em.getReference(Persona.class, 20)).thenReturn(newPersona);

            controller.edit(inputUsuario);

            verify(em).merge(oldPersona);
            verify(em).merge(newPersona);
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
            Usuario usuario = new Usuario(id, "u", "p", true, "user");
            usuario.setRegistroAuditoriaList(new ArrayList<>());
            usuario.setFkIdPersona(null);

            when(em.getReference(Usuario.class, id)).thenReturn(usuario);

            controller.destroy(id);

            verify(em).remove(usuario);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw IllegalOrphanException when audit entries exist")
        void shouldThrowOnOrphan() {
            Integer id = 1;
            RegistroAuditoria ra = new RegistroAuditoria(99);
            List<RegistroAuditoria> orphans = new ArrayList<>();
            orphans.add(ra);

            Usuario usuario = new Usuario(id, "u", "p", true, "user");
            usuario.setRegistroAuditoriaList(orphans);

            when(em.getReference(Usuario.class, id)).thenReturn(usuario);

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
            when(em.getReference(Usuario.class, id)).thenThrow(new EntityNotFoundException("gone"));

            assertThatThrownBy(() -> controller.destroy(id))
                    .isInstanceOf(NonexistentEntityException.class)
                    .hasMessageContaining("no longer exists");

            verify(em).close();
        }

        @Test
        @DisplayName("should remove FK persona reference on destroy")
        void shouldRemoveFkPersonaRef() throws Exception {
            Integer id = 1;
            Persona persona = new Persona(10);
            persona.setUsuariosList(new ArrayList<>());
            persona.getUsuariosList().add(new Usuario(id));

            Usuario usuario = new Usuario(id, "u", "p", true, "user");
            usuario.setRegistroAuditoriaList(new ArrayList<>());
            usuario.setFkIdPersona(persona);

            when(em.getReference(Usuario.class, id)).thenReturn(usuario);

            controller.destroy(id);

            verify(em).merge(persona);
            verify(em).remove(usuario);
            verify(tx).commit();
            verify(em).close();
        }
    }

    // -- find methods -----------------------------------------------------

    @Nested
    @DisplayName("findUsuariosEntities")
    class FindUsuariosEntities {
        @Test
        @DisplayName("should return all")
        void shouldFindAll() {
            List<Usuario> expected = new ArrayList<>();
            expected.add(new Usuario(1, "a", "p", true, "user"));
            when(em.createQuery("select object(o) from Usuarios as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<Usuario> result = controller.findUsuariosEntities();

            assertThat(result).hasSize(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated")
        void shouldFindPaginated() {
            when(em.createQuery("select object(o) from Usuarios as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            controller.findUsuariosEntities(5, 2);

            verify(query).setMaxResults(5);
            verify(query).setFirstResult(2);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findUsuarios")
    class FindUsuarios {
        @Test
        @DisplayName("should find by id")
        void shouldFindById() {
            when(em.find(Usuario.class, 1)).thenReturn(new Usuario(1));

            Usuario result = controller.findUsuarios(1);

            assertThat(result.getIdUsuario()).isEqualTo(1);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getUsuariosCount")
    class GetUsuariosCount {
        @Test
        @DisplayName("should return count")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from Usuarios as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(5L);

            int result = controller.getUsuariosCount();

            assertThat(result).isEqualTo(5);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("buscarUsuarios")
    class BuscarUsuarios {
        @Test
        @DisplayName("should use named query")
        void shouldUseNamedQuery() {
            List<Usuario> expected = new ArrayList<>();
            expected.add(new Usuario(1));
            when(em.createNamedQuery("Usuario.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<Usuario> result = controller.buscarUsuarios();

            assertThat(result).hasSize(1);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("findUsuarioByPersona")
    class FindUsuarioByPersona {
        @Test
        @DisplayName("should return null when idPersona is null")
        void shouldReturnNullWhenNullArg() {
            Usuario result = controller.findUsuarioByPersona(null);
            assertThat(result).isNull();
            // No EM created at all
            verify(emf, never()).createEntityManager();
        }

        @Test
        @DisplayName("should return usuario when found")
        void shouldReturnFound() {
            Usuario expected = new Usuario(1);
            List<Usuario> results = new ArrayList<>();
            results.add(expected);
            when(em.createNamedQuery("Usuario.findByFkIdPersona")).thenReturn(query);
            when(query.getResultList()).thenReturn(results);

            Usuario result = controller.findUsuarioByPersona(10);

            assertThat(result).isSameAs(expected);
            verify(query).setParameter("idPersona", 10);
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(em.createNamedQuery("Usuario.findByFkIdPersona")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            Usuario result = controller.findUsuarioByPersona(10);

            assertThat(result).isNull();
            verify(em).close();
        }
    }

    // -- modificarUsuario -------------------------------------------------

    @Nested
    @DisplayName("modificarUsuario")
    class ModificarUsuario {
        @Test
        @DisplayName("should update entity fields when version matches")
        void shouldUpdateWhenVersionMatches() throws Exception {
            Integer id = 1;
            Usuario persistentUsuario = new Usuario(id, "old", "oldp", true, "user");
            persistentUsuario.setVersion(0);

            Usuario inputUsuario = new Usuario(id, "new", "newp", false, "admin");
            inputUsuario.setVersion(0);

            when(em.find(Usuario.class, id)).thenReturn(persistentUsuario);

            Boolean result = controller.modificarUsuario(inputUsuario);

            assertThat(result).isTrue();
            assertThat(persistentUsuario.getNombre()).isEqualTo("new");
            assertThat(persistentUsuario.getContrasenia()).isEqualTo("newp");
            assertThat(persistentUsuario.getEstado()).isFalse();
            assertThat(persistentUsuario.getTipo()).isEqualTo("admin");
            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassModifiedException when version differs")
        void shouldThrowOnVersionMismatch() {
            Integer id = 1;
            Usuario persistentUsuario = new Usuario(id, "u", "p", true, "user");
            persistentUsuario.setVersion(5);

            Usuario inputUsuario = new Usuario(id, "u", "p", true, "user");
            inputUsuario.setVersion(0);

            when(em.find(Usuario.class, id)).thenReturn(persistentUsuario);

            assertThatThrownBy(() -> controller.modificarUsuario(inputUsuario))
                    .isInstanceOf(ClassModifiedException.class);

            verify(em).close();
        }

        @Test
        @DisplayName("should throw ClassEliminatedException when entity not found")
        void shouldThrowWhenNotFound() {
            Integer id = 999;
            Usuario inputUsuario = new Usuario(id, "u", "p", true, "user");

            when(em.find(Usuario.class, id)).thenReturn(null);

            assertThatThrownBy(() -> controller.modificarUsuario(inputUsuario))
                    .isInstanceOf(ClassEliminatedException.class);

            verify(em).close();
        }
    }

    // -- getNombreJpa -----------------------------------------------------

    @Nested
    @DisplayName("getNombreJpa")
    class GetNombreJpa {
        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            assertThat(controller.getNombreJpa()).isEqualTo(UsuarioJpaController.class.getName());
        }
    }
}
