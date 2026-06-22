package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.*;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.negocio.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Batch tests for the remaining ~20 JPA controllers. */
@DisplayName("Remaining JpaControllers unit tests")
class RemainingControllersJpaTest {

    protected EntityManagerFactory mockEmf;
    protected EntityManager mockEm;
    protected EntityTransaction mockTx;
    protected UserTransaction mockUtx;
    protected Query mockQuery;

    @BeforeEach
    void setUp() {
        mockEmf = mock(EntityManagerFactory.class);
        mockEm = mock(EntityManager.class);
        mockTx = mock(EntityTransaction.class);
        mockUtx = mock(UserTransaction.class);
        mockQuery = mock(Query.class);

        when(mockEmf.createEntityManager()).thenReturn(mockEm);
        when(mockEm.getTransaction()).thenReturn(mockTx);
        when(mockEm.createQuery(anyString())).thenReturn(mockQuery);
    }

    @AfterEach
    void tearDown() {
        // No-op
    }

    /** Helper to instantiate a standard controller with (UserTransaction, EntityManagerFactory) */
    protected <T> T createStandardController(Class<T> controllerClass) {
        try {
            Constructor<T> ctor = controllerClass.getConstructor(UserTransaction.class, EntityManagerFactory.class);
            return ctor.newInstance(mockUtx, mockEmf);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + controllerClass.getSimpleName(), e);
        }
    }

    /** Helper to instantiate using reflection for private constructors */
    protected <T> T createWithReflection(Class<T> clazz, Object... args) {
        try {
            for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
                if (ctor.getParameterCount() == args.length) {
                    ctor.setAccessible(true);
                    return clazz.cast(ctor.newInstance(args));
                }
            }
            throw new IllegalArgumentException("No constructor with " + args.length + " args for " + clazz.getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName() + " with reflection", e);
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  Helper for creating simple test entities via reflection                   ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    /** Creates an entity instance with the given id set via setId method */
    protected <T> T createEntityWithId(Class<T> entityClass, Integer id) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();
            entityClass.getMethod("setId", Integer.class).invoke(entity, id);
            entityClass.getMethod("setVersion", int.class).invoke(entity, 0);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + entityClass.getSimpleName() + " with id", e);
        }
    }

    protected <T> T createEntityWithId(Class<T> entityClass, Class<?> idType, Integer id) {
        try {
            T inst = entityClass.getDeclaredConstructor().newInstance();
            // Try setId(Integer) first, then setId(int)
            try {
                entityClass.getMethod("setId", idType).invoke(inst, id);
            } catch (NoSuchMethodException e) {
                entityClass.getMethod("setId", int.class).invoke(inst, id);
            }
            return inst;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create " + entityClass.getSimpleName(), e);
        }
    }

    /** Gets the actual class name for the entity this controller manages */
    protected <T> String getControllerName(Class<T> controllerClass) {
        return controllerClass.getSimpleName();
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  STANDARD CONTROLLERS — all 18 share identical CRUD/query patterns          ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("Standard controllers: Copia, DocumentoPresentado, Escritura, EstadoDeGestion, Folio, " +
            "GestionDeEscritura, Historial, Inmueble, Item, MovimientoTestimonio, Pago, PlantillaPresupuesto, " +
            "PlantillaTramite, Presupuesto, Suplencia, Testimonio, TipoDeDocumento, TipoDeFolio, TipoDeTramite")
    class StandardControllers {

        @Nested
        @DisplayName("CopiaJpaController")
        class CopiaTest {
            private CopiaJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(CopiaJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa returns class name")
            void getNombreJpa() {
                assertThat(controller.getNombreJpa()).contains("CopiaJpaController");
            }

            @Test
            @DisplayName("getEntityManager returns non-null EM")
            void getEntityManager() {
                assertThat(controller.getEntityManager()).isSameAs(mockEm);
            }

            @Test
            @DisplayName("findCopiaEntities returns list from query")
            void findCopiaEntities() {
                Copia mockEntity = mock(Copia.class);
                when(mockQuery.getResultList()).thenReturn(List.of(mockEntity));

                List<Copia> result = controller.findCopiaEntities();
                assertThat(result).containsExactly(mockEntity);
                verify(mockEm).createQuery("SELECT c FROM Copia c");
                verify(mockQuery).getResultList();
            }

            @Test
            @DisplayName("findCopiaEntities paginated returns limited list")
            void findCopiaEntitiesPaginated() {
                Copia mockEntity = mock(Copia.class);
                when(mockQuery.getResultList()).thenReturn(List.of(mockEntity));

                List<Copia> result = controller.findCopiaEntities(10, 0);
                assertThat(result).containsExactly(mockEntity);
                verify(mockQuery).setMaxResults(10);
                verify(mockQuery).setFirstResult(0);
            }

            @Test
            @DisplayName("findCopia returns entity from EM")
            void findCopia() {
                Copia mockEntity = mock(Copia.class);
                when(mockEm.find(Copia.class, 1)).thenReturn(mockEntity);

                Copia result = controller.findCopia(1);
                assertThat(result).isSameAs(mockEntity);
            }

            @Test
            @DisplayName("getCopiaCount returns count from query")
            void getCopiaCount() {
                when(mockQuery.getSingleResult()).thenReturn(42L);

                int count = controller.getCopiaCount();
                assertThat(count).isEqualTo(42);
                verify(mockEm).createQuery("SELECT count(c) FROM Copia c");
            }

            @Test
            @DisplayName("create persists entity")
            void create() throws Exception {
                Copia entity = new Copia();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).begin();
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("create rolls back on exception")
            void createRollback() {
                Copia entity = new Copia();
                doThrow(new RuntimeException("DB error")).when(mockEm).persist(entity);

                assertThatThrownBy(() -> controller.create(entity))
                        .isInstanceOf(Exception.class);
                verify(mockTx).begin();
                verify(mockTx).rollback();
            }

            @Test
            @DisplayName("destroy removes entity")
            void destroy() throws Exception {
                Copia entity = new Copia();
                when(mockEm.find(Copia.class, 1)).thenReturn(entity);

                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy throws NonexistentEntityException when not found")
            void destroyNotFound() {
                when(mockEm.find(Copia.class, 99)).thenReturn(null);

                assertThatThrownBy(() -> controller.destroy(99))
                        .isInstanceOf(Exception.class);
                verify(mockTx).begin();
                verify(mockTx).rollback();
            }
        }

        @Nested
        @DisplayName("DocumentoPresentadoJpaController")
        class DocumentoPresentadoTest {
            private DocumentoPresentadoJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(DocumentoPresentadoJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNombreJpa() {
                assertThat(controller.getNombreJpa()).contains("DocumentoPresentadoJpaController");
            }

            @Test
            @DisplayName("findDocumentoPresentadoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Documentopresentado.class)));
                assertThat(controller.findDocumentoPresentadoEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findDocumentoPresentado")
            void findById() {
                when(mockEm.find(Documentopresentado.class, 5)).thenReturn(mock(Documentopresentado.class));
                assertThat(controller.findDocumentoPresentado(5)).isNotNull();
            }

            @Test
            @DisplayName("getDocumentoPresentadoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(7L);
                assertThat(controller.getDocumentoPresentadoCount()).isEqualTo(7);
            }

            @Test
            @DisplayName("create persists and commits")
            void create() throws Exception {
                Documentopresentado entity = new Documentopresentado();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy finds and removes")
            void destroy() throws Exception {
                Documentopresentado entity = new Documentopresentado();
                when(mockEm.find(Documentopresentado.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("EscrituraJpaController")
        class EscrituraTest {
            private EscrituraJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(EscrituraJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("EscrituraJpaController"); }

            @Test
            @DisplayName("findEscrituraEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Escritura.class)));
                assertThat(controller.findEscrituraEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findEscritura")
            void findById() {
                when(mockEm.find(Escritura.class, 3)).thenReturn(mock(Escritura.class));
                assertThat(controller.findEscritura(3)).isNotNull();
            }

            @Test
            @DisplayName("getEscrituraCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(15L);
                assertThat(controller.getEscrituraCount()).isEqualTo(15);
            }

            @Test
            @DisplayName("create persists and commits")
            void create() throws Exception {
                Escritura entity = new Escritura();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy finds and removes")
            void destroy() throws Exception {
                Escritura entity = new Escritura();
                when(mockEm.find(Escritura.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("EstadoDeGestionJpaController")
        class EstadoDeGestionTest {
            private EstadoDeGestionJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(EstadoDeGestionJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("EstadoDeGestionJpaController"); }

            @Test
            @DisplayName("findEstadoDeGestionEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Estadodegestion.class)));
                assertThat(controller.findEstadoDeGestionEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findEstadoDeGestion")
            void findById() {
                when(mockEm.find(Estadodegestion.class, 2)).thenReturn(mock(Estadodegestion.class));
                assertThat(controller.findEstadoDeGestion(2)).isNotNull();
            }

            @Test
            @DisplayName("getEstadoDeGestionCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(5L);
                assertThat(controller.getEstadoDeGestionCount()).isEqualTo(5);
            }

            @Test
            @DisplayName("create persists")
            void create() throws Exception {
                Estadodegestion entity = new Estadodegestion();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy removes")
            void destroy() throws Exception {
                Estadodegestion entity = new Estadodegestion();
                when(mockEm.find(Estadodegestion.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("FolioJpaController")
        class FolioTest {
            private FolioJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(FolioJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("FolioJpaController"); }

            @Test @DisplayName("findFolioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Folio.class)));
                assertThat(controller.findFolioEntities()).hasSize(1);
            }

            @Test @DisplayName("findFolio")
            void findById() {
                when(mockEm.find(Folio.class, 4)).thenReturn(mock(Folio.class));
                assertThat(controller.findFolio(4)).isNotNull();
            }

            @Test @DisplayName("getFolioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(3L);
                assertThat(controller.getFolioCount()).isEqualTo(3);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Folio entity = new Folio();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Folio entity = new Folio();
                when(mockEm.find(Folio.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("GestionDeEscrituraJpaController")
        class GestionDeEscrituraTest {
            private GestionDeEscrituraJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(GestionDeEscrituraJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("GestionDeEscrituraJpaController"); }

            @Test @DisplayName("findGestionDeEscrituraEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Gestiondeescritura.class)));
                assertThat(controller.findGestionDeEscrituraEntities()).hasSize(1);
            }

            @Test @DisplayName("findGestionDeEscritura")
            void findById() {
                when(mockEm.find(Gestiondeescritura.class, 7)).thenReturn(mock(Gestiondeescritura.class));
                assertThat(controller.findGestionDeEscritura(7)).isNotNull();
            }

            @Test @DisplayName("getGestionDeEscrituraCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(2L);
                assertThat(controller.getGestionDeEscrituraCount()).isEqualTo(2);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Gestiondeescritura entity = new Gestiondeescritura();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Gestiondeescritura entity = new Gestiondeescritura();
                when(mockEm.find(Gestiondeescritura.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("HistorialJpaController")
        class HistorialTest {
            private HistorialJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(HistorialJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("HistorialJpaController"); }

            @Test @DisplayName("findHistorialEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Historial.class)));
                assertThat(controller.findHistorialEntities()).hasSize(1);
            }

            @Test @DisplayName("findHistorial")
            void findById() {
                when(mockEm.find(Historial.class, 2)).thenReturn(mock(Historial.class));
                assertThat(controller.findHistorial(2)).isNotNull();
            }

            @Test @DisplayName("getHistorialCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(10L);
                assertThat(controller.getHistorialCount()).isEqualTo(10);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Historial entity = new Historial();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Historial entity = new Historial();
                when(mockEm.find(Historial.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("InmuebleJpaController")
        class InmuebleTest {
            private InmuebleJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(InmuebleJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("InmuebleJpaController"); }

            @Test @DisplayName("findInmuebleEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Inmueble.class)));
                assertThat(controller.findInmuebleEntities()).hasSize(1);
            }

            @Test @DisplayName("findInmueble")
            void findById() {
                when(mockEm.find(Inmueble.class, 3)).thenReturn(mock(Inmueble.class));
                assertThat(controller.findInmueble(3)).isNotNull();
            }

            @Test @DisplayName("getInmuebleCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(6L);
                assertThat(controller.getInmuebleCount()).isEqualTo(6);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Inmueble entity = new Inmueble();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Inmueble entity = new Inmueble();
                when(mockEm.find(Inmueble.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("ItemJpaController")
        class ItemTest {
            private ItemJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(ItemJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("ItemJpaController"); }

            @Test @DisplayName("findItemEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Item.class)));
                assertThat(controller.findItemEntities()).hasSize(1);
            }

            @Test @DisplayName("findItem")
            void findById() {
                when(mockEm.find(Item.class, 8)).thenReturn(mock(Item.class));
                assertThat(controller.findItem(8)).isNotNull();
            }

            @Test @DisplayName("getItemCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(4L);
                assertThat(controller.getItemCount()).isEqualTo(4);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Item entity = new Item();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Item entity = new Item();
                when(mockEm.find(Item.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("MovimientoTestimonioJpaController")
        class MovimientoTestimonioTest {
            private MovimientoTestimonioJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(MovimientoTestimonioJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("MovimientoTestimonioJpaController"); }

            @Test @DisplayName("findMovimientoTestimonioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Movimientotestimonio.class)));
                assertThat(controller.findMovimientoTestimonioEntities()).hasSize(1);
            }

            @Test @DisplayName("findMovimientoTestimonio")
            void findById() {
                when(mockEm.find(Movimientotestimonio.class, 5)).thenReturn(mock(Movimientotestimonio.class));
                assertThat(controller.findMovimientoTestimonio(5)).isNotNull();
            }

            @Test @DisplayName("getMovimientoTestimonioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(9L);
                assertThat(controller.getMovimientoTestimonioCount()).isEqualTo(9);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Movimientotestimonio entity = new Movimientotestimonio();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Movimientotestimonio entity = new Movimientotestimonio();
                when(mockEm.find(Movimientotestimonio.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PagoJpaController")
        class PagoTest {
            private PagoJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PagoJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("PagoJpaController"); }

            @Test @DisplayName("findPagoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Pago.class)));
                assertThat(controller.findPagoEntities()).hasSize(1);
            }

            @Test @DisplayName("findPago")
            void findById() {
                when(mockEm.find(Pago.class, 6)).thenReturn(mock(Pago.class));
                assertThat(controller.findPago(6)).isNotNull();
            }

            @Test @DisplayName("getPagoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(11L);
                assertThat(controller.getPagoCount()).isEqualTo(11);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Pago entity = new Pago();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Pago entity = new Pago();
                when(mockEm.find(Pago.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PlantillaPresupuestoJpaController")
        class PlantillaPresupuestoTest {
            private PlantillaPresupuestoJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PlantillaPresupuestoJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("PlantillaPresupuestoJpaController"); }

            @Test @DisplayName("findPlantillaPresupuestoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Plantillapresupuesto.class)));
                assertThat(controller.findPlantillaPresupuestoEntities()).hasSize(1);
            }

            @Test @DisplayName("findPlantillaPresupuesto")
            void findById() {
                when(mockEm.find(Plantillapresupuesto.class, 2)).thenReturn(mock(Plantillapresupuesto.class));
                assertThat(controller.findPlantillaPresupuesto(2)).isNotNull();
            }

            @Test @DisplayName("getPlantillaPresupuestoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(8L);
                assertThat(controller.getPlantillaPresupuestoCount()).isEqualTo(8);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Plantillapresupuesto entity = new Plantillapresupuesto();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Plantillapresupuesto entity = new Plantillapresupuesto();
                when(mockEm.find(Plantillapresupuesto.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PlantillaTramiteJpaController")
        class PlantillaTramiteTest {
            private PlantillaTramiteJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PlantillaTramiteJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("PlantillaTramiteJpaController"); }

            @Test @DisplayName("findPlantillaTramiteEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Plantillatramite.class)));
                assertThat(controller.findPlantillaTramiteEntities()).hasSize(1);
            }

            @Test @DisplayName("findPlantillaTramite")
            void findById() {
                when(mockEm.find(Plantillatramite.class, 4)).thenReturn(mock(Plantillatramite.class));
                assertThat(controller.findPlantillaTramite(4)).isNotNull();
            }

            @Test @DisplayName("getPlantillaTramiteCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(12L);
                assertThat(controller.getPlantillaTramiteCount()).isEqualTo(12);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Plantillatramite entity = new Plantillatramite();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Plantillatramite entity = new Plantillatramite();
                when(mockEm.find(Plantillatramite.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PresupuestoJpaController")
        class PresupuestoTest {
            private PresupuestoJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PresupuestoJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("PresupuestoJpaController"); }

            @Test @DisplayName("findPresupuestoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Presupuesto.class)));
                assertThat(controller.findPresupuestoEntities()).hasSize(1);
            }

            @Test @DisplayName("findPresupuesto")
            void findById() {
                when(mockEm.find(Presupuesto.class, 9)).thenReturn(mock(Presupuesto.class));
                assertThat(controller.findPresupuesto(9)).isNotNull();
            }

            @Test @DisplayName("getPresupuestoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(20L);
                assertThat(controller.getPresupuestoCount()).isEqualTo(20);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Presupuesto entity = new Presupuesto();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Presupuesto entity = new Presupuesto();
                when(mockEm.find(Presupuesto.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("findPresupuestoByTramite uses named query")
            void findByTramite() {
                when(mockEm.createNamedQuery("Presupuesto.findByTramite")).thenReturn(mockQuery);
                when(mockQuery.setParameter(eq("idTramite"), anyInt())).thenReturn(mockQuery);
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Presupuesto.class)));

                List<Presupuesto> result = controller.findPresupuestoByTramite(1);
                assertThat(result).hasSize(1);
                verify(mockEm).createNamedQuery("Presupuesto.findByTramite");
                verify(mockQuery).setParameter("idTramite", 1);
            }
        }

        @Nested
        @DisplayName("SuplenciaJpaController")
        class SuplenciaTest {
            private SuplenciaJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(SuplenciaJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("SuplenciaJpaController"); }

            @Test @DisplayName("findSuplenciaEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Suplencia.class)));
                assertThat(controller.findSuplenciaEntities()).hasSize(1);
            }

            @Test @DisplayName("findSuplencia")
            void findById() {
                when(mockEm.find(Suplencia.class, 3)).thenReturn(mock(Suplencia.class));
                assertThat(controller.findSuplencia(3)).isNotNull();
            }

            @Test @DisplayName("getSuplenciaCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(1L);
                assertThat(controller.getSuplenciaCount()).isEqualTo(1);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Suplencia entity = new Suplencia();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Suplencia entity = new Suplencia();
                when(mockEm.find(Suplencia.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TestimonioJpaController")
        class TestimonioTest {
            private TestimonioJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TestimonioJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TestimonioJpaController"); }

            @Test @DisplayName("findTestimonioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Testimonio.class)));
                assertThat(controller.findTestimonioEntities()).hasSize(1);
            }

            @Test @DisplayName("findTestimonio")
            void findById() {
                when(mockEm.find(Testimonio.class, 7)).thenReturn(mock(Testimonio.class));
                assertThat(controller.findTestimonio(7)).isNotNull();
            }

            @Test @DisplayName("getTestimonioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(3L);
                assertThat(controller.getTestimonioCount()).isEqualTo(3);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Testimonio entity = new Testimonio();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Testimonio entity = new Testimonio();
                when(mockEm.find(Testimonio.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeDocumentoJpaController")
        class TipoDeDocumentoTest {
            private TipoDeDocumentoJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TipoDeDocumentoJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TipoDeDocumentoJpaController"); }

            @Test @DisplayName("findTipoDeDocumentoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Tipodedocumento.class)));
                assertThat(controller.findTipoDeDocumentoEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeDocumento")
            void findById() {
                when(mockEm.find(Tipodedocumento.class, 4)).thenReturn(mock(Tipodedocumento.class));
                assertThat(controller.findTipoDeDocumento(4)).isNotNull();
            }

            @Test @DisplayName("getTipoDeDocumentoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(16L);
                assertThat(controller.getTipoDeDocumentoCount()).isEqualTo(16);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Tipodedocumento entity = new Tipodedocumento();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Tipodedocumento entity = new Tipodedocumento();
                when(mockEm.find(Tipodedocumento.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeFolioJpaController")
        class TipoDeFolioTest {
            private TipoDeFolioJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TipoDeFolioJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TipoDeFolioJpaController"); }

            @Test @DisplayName("findTipoDeFolioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Tipodefolio.class)));
                assertThat(controller.findTipoDeFolioEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeFolio")
            void findById() {
                when(mockEm.find(Tipodefolio.class, 5)).thenReturn(mock(Tipodefolio.class));
                assertThat(controller.findTipoDeFolio(5)).isNotNull();
            }

            @Test @DisplayName("getTipoDeFolioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(2L);
                assertThat(controller.getTipoDeFolioCount()).isEqualTo(2);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Tipodefolio entity = new Tipodefolio();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Tipodefolio entity = new Tipodefolio();
                when(mockEm.find(Tipodefolio.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeTramiteJpaController")
        class TipoDeTramiteTest {
            private TipoDeTramiteJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TipoDeTramiteJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TipoDeTramiteJpaController"); }

            @Test @DisplayName("findTipoDeTramiteEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Tipodetramite.class)));
                assertThat(controller.findTipoDeTramiteEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeTramite")
            void findById() {
                when(mockEm.find(Tipodetramite.class, 6)).thenReturn(mock(Tipodetramite.class));
                assertThat(controller.findTipoDeTramite(6)).isNotNull();
            }

            @Test @DisplayName("getTipoDeTramiteCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(7L);
                assertThat(controller.getTipoDeTramiteCount()).isEqualTo(7);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Tipodetramite entity = new Tipodetramite();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Tipodetramite entity = new Tipodetramite();
                when(mockEm.find(Tipodetramite.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  TIPOIDENTIFICACION — singleton with private constructor                    ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("TipoIdentificacionJpaController (singleton)")
    class TipoIdentificacionSingletonTest {

        @Test
        @DisplayName("getNombreJpa via singleton pattern")
        void getNombreJpa() {
            // Re-create the EMF for this test because TipoIdentificacionJpaController.getEmf()
            // gets checked via getInstance
            java.lang.reflect.Field emfField;
            try {
                emfField = TipoIdentificacionJpaController.class.getDeclaredField("emf");
                emfField.setAccessible(true);
                emfField.set(null, mockEmf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            TipoIdentificacionJpaController controller = TipoIdentificacionJpaController.getInstance();
            assertThat(controller.getNombreJpa()).contains("TipoIdentificacionJpaController");
        }

        @Test
        @DisplayName("getInstance returns singleton")
        void getInstanceSingleton() {
            java.lang.reflect.Field emfField;
            try {
                emfField = TipoIdentificacionJpaController.class.getDeclaredField("emf");
                emfField.setAccessible(true);
                emfField.set(null, mockEmf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            TipoIdentificacionJpaController first = TipoIdentificacionJpaController.getInstance();
            TipoIdentificacionJpaController second = TipoIdentificacionJpaController.getInstance();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("create persists and commits")
        void create() throws Exception {
            java.lang.reflect.Field emfField;
            try {
                emfField = TipoIdentificacionJpaController.class.getDeclaredField("emf");
                emfField.setAccessible(true);
                emfField.set(null, mockEmf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            TipoIdentificacionJpaController controller = TipoIdentificacionJpaController.getInstance();
            Tipoidentificacion entity = new Tipoidentificacion();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("findTipoIdentificacionEntities")
        void findEntities() {
            java.lang.reflect.Field emfField;
            try {
                emfField = TipoIdentificacionJpaController.class.getDeclaredField("emf");
                emfField.setAccessible(true);
                emfField.set(null, mockEmf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tipoidentificacion.class)));
            TipoIdentificacionJpaController controller = TipoIdentificacionJpaController.getInstance();
            assertThat(controller.findTipoIdentificacionEntities()).hasSize(1);
        }

        @Test
        @DisplayName("getTipoIdentificacionCount")
        void getCount() {
            java.lang.reflect.Field emfField;
            try {
                emfField = TipoIdentificacionJpaController.class.getDeclaredField("emf");
                emfField.setAccessible(true);
                emfField.set(null, mockEmf);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            when(mockQuery.getSingleResult()).thenReturn(5L);
            TipoIdentificacionJpaController controller = TipoIdentificacionJpaController.getInstance();
            assertThat(controller.getTipoIdentificacionCount()).isEqualTo(5);
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  PERSONA — singleton with private constructor, has extra query methods       ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("PersonaJpaController (singleton)")
    class PersonaSingletonTest {
        private PersonaJpaController controller;

        @BeforeEach
        void setUp() throws Exception {
            // PersonaJpaController has a private constructor — use reflection
            controller = createWithReflection(PersonaJpaController.class, mockUtx, mockEmf);
        }

        @Test
        @DisplayName("getNombreJpa")
        void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("PersonaJpaController"); }

        @Test
        @DisplayName("findPersonaEntities")
        void findEntities() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Persona.class)));
            assertThat(controller.findPersonaEntities()).hasSize(1);
        }

        @Test
        @DisplayName("findPersonaEntities paginated")
        void findEntitiesPaginated() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Persona.class)));
            assertThat(controller.findPersonaEntities(20, 0)).hasSize(1);
            verify(mockQuery).setMaxResults(20);
            verify(mockQuery).setFirstResult(0);
        }

        @Test
        @DisplayName("findPersona")
        void findById() {
            when(mockEm.find(Persona.class, 10)).thenReturn(mock(Persona.class));
            assertThat(controller.findPersona(10)).isNotNull();
        }

        @Test
        @DisplayName("getPersonaCount")
        void getCount() {
            when(mockQuery.getSingleResult()).thenReturn(50L);
            assertThat(controller.getPersonaCount()).isEqualTo(50);
        }

        @Test
        @DisplayName("create persists")
        void create() throws Exception {
            Persona entity = new Persona();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("destroy removes")
        void destroy() throws Exception {
            Persona entity = new Persona();
            when(mockEm.find(Persona.class, 1)).thenReturn(entity);
            controller.destroy(1);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("findPersonaTipoNumeroIdentificacion uses named query")
        void findTipoNumeroIdentificacion() {
            // DtoPersona needs DtoTipoIdentificacion — create a minimal mock setup
            // ControllerNegocio.getInstancia() call will be handled via the mock DtoPersona
            when(mockEm.createNamedQuery("Persona.findByNumeroIdentificacion")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("numeroIdentificacion"), anyString())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(new ArrayList<>());

            DtoPersona dto = new DtoPersona();
            com.licensis.notaire.dto.DtoTipoIdentificacion dtoTipo = new com.licensis.notaire.dto.DtoTipoIdentificacion();
            dto.setDtoTipoIdentificacion(dtoTipo);
            dto.setNumeroIdentificacion("12345");

            // With empty result list, method returns null
            Persona result = controller.findPersonaTipoNumeroIdentificacion(dto);
            assertThat(result).isNull();
            verify(mockQuery).setParameter("numeroIdentificacion", "12345");
        }

        @Test
        @DisplayName("findPersonaNombreApellido uses named query")
        void findNombreApellido() {
            when(mockEm.createNamedQuery("Persona.findByPersonaNombreApellido")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("nombre"), anyString())).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("apellido"), anyString())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Persona.class)));

            DtoPersona dto = new DtoPersona();
            dto.setNombre("Juan");
            dto.setApellido("Perez");
            List<Persona> result = controller.findPersonaNombreApellido(dto);
            assertThat(result).hasSize(1);
            verify(mockQuery).setParameter("nombre", "%Juan%");
            verify(mockQuery).setParameter("apellido", "%Perez%");
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  TRAMITE — standard constructor + 4 named queries                          ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("TramiteJpaController")
    class TramiteTest {
        private TramiteJpaController controller;

        @BeforeEach
        void setUp() {
            controller = createStandardController(TramiteJpaController.class);
        }

        @Test @DisplayName("getNombreJpa")
        void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TramiteJpaController"); }

        @Test @DisplayName("findTramiteEntities")
        void findEntities() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tramite.class)));
            assertThat(controller.findTramiteEntities()).hasSize(1);
        }

        @Test @DisplayName("findTramite")
        void findById() {
            when(mockEm.find(Tramite.class, 1)).thenReturn(mock(Tramite.class));
            assertThat(controller.findTramite(1)).isNotNull();
        }

        @Test @DisplayName("getTramiteCount")
        void getCount() {
            when(mockQuery.getSingleResult()).thenReturn(25L);
            assertThat(controller.getTramiteCount()).isEqualTo(25);
        }

        @Test @DisplayName("create persists")
        void create() throws Exception {
            Tramite entity = new Tramite();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test @DisplayName("destroy removes")
        void destroy() throws Exception {
            Tramite entity = new Tramite();
            when(mockEm.find(Tramite.class, 1)).thenReturn(entity);
            controller.destroy(1);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }

        @Test @DisplayName("findTramiteByNumero uses named query")
        void findByNumero() {
            when(mockEm.createNamedQuery("Tramite.findByNumeroTramite")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("numeroTramite"), anyString())).thenReturn(mockQuery);
            when(mockQuery.getSingleResult()).thenReturn(mock(Tramite.class));

            Tramite result = controller.findTramiteByNumero("T-001");
            assertThat(result).isNotNull();
            verify(mockQuery).setParameter("numeroTramite", "T-001");
        }

        @Test
        @DisplayName("findTramitePersonasList uses named query")
        void findPersonasList() {
            when(mockEm.createNamedQuery("Tramite.findTramitesPersonas")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("idTramite"), anyInt())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tramite.class)));

            List<Tramite> result = controller.findTramitePersonasList(5);
            assertThat(result).hasSize(1);
            verify(mockQuery).setParameter("idTramite", 5);
        }

        @Test
        @DisplayName("findTramiteListByPersona uses named query")
        void findListByPersona() {
            when(mockEm.createNamedQuery("Tramite.findTramitesByPersona")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("idPersona"), anyInt())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tramite.class)));

            List<Tramite> result = controller.findTramiteListByPersona(3);
            assertThat(result).hasSize(1);
            verify(mockQuery).setParameter("idPersona", 3);
        }

        @Test
        @DisplayName("findTramiteByEstado uses named query")
        void findByEstado() {
            when(mockEm.createNamedQuery("Tramite.findByEstado")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("estado"), anyString())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tramite.class)));

            List<Tramite> result = controller.findTramiteByEstado("activo");
            assertThat(result).hasSize(1);
            verify(mockQuery).setParameter("estado", "activo");
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  TRAMITESPERSONAS — special constructor: (EntityManagerFactory) only        ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("TramitesPersonasJpaController (single-arg constructor)")
    class TramitesPersonasTest {
        private TramitesPersonasJpaController controller;

        @BeforeEach
        void setUp() {
            controller = new TramitesPersonasJpaController(mockEmf);
        }

        @Test @DisplayName("getNombreJpa")
        void getNombreJpa() { assertThat(controller.getNombreJpa()).contains("TramitesPersonasJpaController"); }

        @Test @DisplayName("getEntityManager")
        void getEntityManager() { assertThat(controller.getEntityManager()).isSameAs(mockEm); }

        @Test @DisplayName("findTramitesPersonasEntities")
        void findEntities() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Tramitespersonas.class)));
            assertThat(controller.findTramitesPersonasEntities()).hasSize(1);
        }

        @Test @DisplayName("findTramitesPersonas")
        void findById() {
            when(mockEm.find(Tramitespersonas.class, 2)).thenReturn(mock(Tramitespersonas.class));
            assertThat(controller.findTramitesPersonas(2)).isNotNull();
        }

        @Test @DisplayName("getTramitesPersonasCount")
        void getCount() {
            when(mockQuery.getSingleResult()).thenReturn(8L);
            assertThat(controller.getTramitesPersonasCount()).isEqualTo(8);
        }

        @Test @DisplayName("create persists")
        void create() throws Exception {
            Tramitespersonas entity = new Tramitespersonas();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test @DisplayName("destroy removes")
        void destroy() throws Exception {
            Tramitespersonas entity = new Tramitespersonas();
            when(mockEm.find(Tramitespersonas.class, 1)).thenReturn(entity);
            controller.destroy(1);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }
    }
}
