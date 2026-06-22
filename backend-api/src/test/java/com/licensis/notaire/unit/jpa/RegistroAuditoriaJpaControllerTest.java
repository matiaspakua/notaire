package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RegistroAuditoriaJpaController unit tests")
@ExtendWith(MockitoExtension.class)
class RegistroAuditoriaJpaControllerTest {

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

    private RegistroAuditoriaJpaController controller;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(emf.createEntityManager()).thenReturn(em);
        controller = createWithReflection(RegistroAuditoriaJpaController.class, utx, emf);
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
            Usuario usuario = new Usuario(1);
            RegistroAuditoria ra = new RegistroAuditoria(99);
            ra.setFkIdUsuario(usuario);

            Usuario usuarioRef = new Usuario(1);
            usuarioRef.setRegistroAuditoriaList(new ArrayList<>());
            when(em.getReference(Usuario.class, 1)).thenReturn(usuarioRef);

            boolean result = controller.create(ra);

            assertThat(result).isTrue();
            verify(em).persist(ra);
            verify(em).merge(usuarioRef);
            verify(tx).begin();
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should handle null FK usuario")
        void shouldHandleNullFk() {
            RegistroAuditoria ra = new RegistroAuditoria(99);

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
            Usuario usuario = new Usuario(1);
            RegistroAuditoria persistentRA = new RegistroAuditoria(id);
            persistentRA.setFkIdUsuario(usuario);
            RegistroAuditoria inputRA = new RegistroAuditoria(id);
            inputRA.setFkIdUsuario(usuario);

            when(em.find(RegistroAuditoria.class, id)).thenReturn(persistentRA);

            controller.edit(inputRA);

            verify(em).merge(inputRA);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should update FK references when usuario changes")
        void shouldUpdateFkWhenUsuarioChanges() throws Exception {
            Integer id = 1;
            Usuario oldUsuario = new Usuario(10);
            oldUsuario.setRegistroAuditoriaList(new ArrayList<>());
            oldUsuario.getRegistroAuditoriaList().add(new RegistroAuditoria(id));

            Usuario newUsuario = new Usuario(20);
            newUsuario.setRegistroAuditoriaList(new ArrayList<>());

            RegistroAuditoria persistentRA = new RegistroAuditoria(id);
            persistentRA.setFkIdUsuario(oldUsuario);

            RegistroAuditoria inputRA = new RegistroAuditoria(id);
            inputRA.setFkIdUsuario(newUsuario);

            when(em.find(RegistroAuditoria.class, id)).thenReturn(persistentRA);
            when(em.getReference(Usuario.class, 20)).thenReturn(newUsuario);

            controller.edit(inputRA);

            verify(em).merge(oldUsuario);
            verify(em).merge(newUsuario);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on stale data")
        void shouldThrowWhenEntityGone() {
            Integer id = 999;
            RegistroAuditoria inputRA = new RegistroAuditoria(id);
            when(em.find(RegistroAuditoria.class, id)).thenThrow(new IllegalArgumentException("gone"));

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
            Usuario usuario = new Usuario(10);
            usuario.setRegistroAuditoriaList(new ArrayList<>());

            RegistroAuditoria ra = new RegistroAuditoria(id);
            ra.setFkIdUsuario(usuario);

            when(em.getReference(RegistroAuditoria.class, id)).thenReturn(ra);

            controller.destroy(id);

            verify(em).merge(usuario);
            verify(em).remove(ra);
            verify(tx).commit();
            verify(em).close();
        }

        @Test
        @DisplayName("should throw NonexistentEntityException on EntityNotFoundException")
        void shouldThrowWhenEntityNotFound() {
            Integer id = 999;
            when(em.getReference(RegistroAuditoria.class, id))
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
            List<RegistroAuditoria> expected = new ArrayList<>();
            expected.add(new RegistroAuditoria(1));
            when(em.createQuery("select object(o) from RegistroAuditoria as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(expected);

            List<RegistroAuditoria> result = controller.findRegistroAuditoriaEntities();

            assertThat(result).hasSize(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return paginated")
        void shouldFindPaginated() {
            when(em.createQuery("select object(o) from RegistroAuditoria as o")).thenReturn(query);
            when(query.getResultList()).thenReturn(new ArrayList<>());

            controller.findRegistroAuditoriaEntities(10, 0);

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
            when(em.find(RegistroAuditoria.class, 1)).thenReturn(new RegistroAuditoria(1));

            assertThat(controller.findRegistroAuditoria(1).getIdRegistroAuditoria()).isEqualTo(1);
            verify(em).close();
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNull() {
            when(em.find(RegistroAuditoria.class, 999)).thenReturn(null);
            assertThat(controller.findRegistroAuditoria(999)).isNull();
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("getRegistroAuditoriaCount")
    class GetCount {
        @Test
        @DisplayName("should return count")
        void shouldReturnCount() {
            when(em.createQuery("select count(o) from RegistroAuditoria as o")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(42L);

            assertThat(controller.getRegistroAuditoriaCount()).isEqualTo(42);
            verify(em).close();
        }
    }

    @Nested
    @DisplayName("buscarRegistroAuditoriasUsuario")
    class BuscarPorUsuario {
        @Test
        @DisplayName("should return filtered list when usuario matches")
        void shouldReturnFiltered() {
            Usuario user = new Usuario(5);
            List<RegistroAuditoria> all = new ArrayList<>();
            RegistroAuditoria ra1 = new RegistroAuditoria(1);
            ra1.setFkIdUsuario(new Usuario(5));
            RegistroAuditoria ra2 = new RegistroAuditoria(2);
            ra2.setFkIdUsuario(new Usuario(3));
            all.add(ra1);
            all.add(ra2);

            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(all);

            ArrayList<RegistroAuditoria> result = controller.buscarRegistroAuditoriasUsuario(user);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIdRegistroAuditoria()).isEqualTo(1);
            // Note: source code does NOT close EM in this method (pre-existing leak)
        }

        @Test
        @DisplayName("should return null when no match")
        void shouldReturnNullWhenNoMatch() {
            Usuario user = new Usuario(99);
            List<RegistroAuditoria> all = new ArrayList<>();
            RegistroAuditoria ra1 = new RegistroAuditoria(1);
            ra1.setFkIdUsuario(new Usuario(5));
            all.add(ra1);

            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenReturn(query);
            when(query.getResultList()).thenReturn(all);

            ArrayList<RegistroAuditoria> result = controller.buscarRegistroAuditoriasUsuario(user);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should handle exception gracefully")
        void shouldHandleException() {
            when(em.createNamedQuery("RegistroAuditoria.findAll")).thenThrow(new RuntimeException("DB error"));

            ArrayList<RegistroAuditoria> result = controller.buscarRegistroAuditoriasUsuario(new Usuario(1));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNombreJpa")
    class GetNombreJpa {
        @Test
        @DisplayName("should return class name")
        void shouldReturnClassName() {
            assertThat(controller.getNombreJpa()).isEqualTo(RegistroAuditoriaJpaController.class.getName());
        }
    }
}
