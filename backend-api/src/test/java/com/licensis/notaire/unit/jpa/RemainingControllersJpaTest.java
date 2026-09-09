package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.*;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.business.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(mockEm.createNamedQuery(anyString())).thenReturn(mockQuery);
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
        class CopyTest {
            private CopyJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(CopyJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa returns class name")
            void getNameJpa() {
                assertThat(controller.getNameJpa()).contains("CopyJpaController");
            }

            @Test
            @DisplayName("getEntityManager returns non-null EM")
            void getEntityManager() {
                assertThat(controller.getEntityManager()).isSameAs(mockEm);
            }

            @Test
            @DisplayName("findCopiaEntities returns list from query")
            void findCopyEntities() {
                Copy mockEntity = mock(Copy.class);
                when(mockQuery.getResultList()).thenReturn(List.of(mockEntity));

                List<Copy> result = controller.findCopyEntities();
                assertThat(result).containsExactly(mockEntity);
                verify(mockEm).createQuery("select object(o) from Copia as o");
                verify(mockQuery).getResultList();
            }

            @Test
            @DisplayName("findCopiaEntities paginated returns limited list")
            void findCopyEntitiesPaginated() {
                Copy mockEntity = mock(Copy.class);
                when(mockQuery.getResultList()).thenReturn(List.of(mockEntity));

                List<Copy> result = controller.findCopyEntities(10, 0);
                assertThat(result).containsExactly(mockEntity);
                verify(mockQuery).setMaxResults(10);
                verify(mockQuery).setFirstResult(0);
            }

            @Test
            @DisplayName("findCopia returns entity from EM")
            void findCopy() {
                Copy mockEntity = mock(Copy.class);
                when(mockEm.find(Copy.class, 1)).thenReturn(mockEntity);

                Copy result = controller.findCopy(1);
                assertThat(result).isSameAs(mockEntity);
            }

            @Test
            @DisplayName("getCopiaCount returns count from query")
            void getCopyCount() {
                when(mockQuery.getSingleResult()).thenReturn(42L);

                int count = controller.getCopyCount();
                assertThat(count).isEqualTo(42);
                verify(mockEm).createQuery("select count(o) from Copia as o");
            }

            @Test
            @DisplayName("create persists entity")
            void create() throws Exception {
                Copy entity = new Copy();
                entity.setIdCopy(1);
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).begin();
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("create propagates exception without committing")
            void createRollback() {
                Copy entity = new Copy();
                doThrow(new RuntimeException("DB error")).when(mockEm).persist(entity);

                assertThatThrownBy(() -> controller.create(entity))
                        .isInstanceOf(RuntimeException.class);
                verify(mockTx).begin();
                verify(mockTx, never()).commit();
            }

            @Test
            @DisplayName("destroy removes entity")
            void destroy() throws Exception {
                Copy entity = new Copy();
                entity.setFolioList(new ArrayList<>());
                when(mockEm.getReference(Copy.class, 1)).thenReturn(entity);

                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy throws NonexistentEntityException when reference not found")
            void destroyNotFound() {
                Copy proxy = mock(Copy.class);
                when(mockEm.getReference(Copy.class, 99)).thenReturn(proxy);
                when(proxy.getIdCopy()).thenThrow(new jakarta.persistence.EntityNotFoundException());

                assertThatThrownBy(() -> controller.destroy(99))
                        .isInstanceOf(com.licensis.notaire.jpa.exceptions.NonexistentEntityException.class);
                verify(mockTx).begin();
            }
        }

        @Nested
        @DisplayName("DocumentoPresentadoJpaController")
        class SubmittedDocumentTest {
            private SubmittedDocumentJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(SubmittedDocumentJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNameJpa() {
                assertThat(controller.getNameJpa()).contains("SubmittedDocumentJpaController");
            }

            @Test
            @DisplayName("findDocumentoPresentadoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(SubmittedDocument.class)));
                assertThat(controller.findSubmittedDocumentEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findDocumentoPresentado")
            void findById() {
                when(mockEm.find(SubmittedDocument.class, 5)).thenReturn(mock(SubmittedDocument.class));
                assertThat(controller.findSubmittedDocument(5)).isNotNull();
            }

            @Test
            @DisplayName("getDocumentoPresentadoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(7L);
                assertThat(controller.getSubmittedDocumentCount()).isEqualTo(7);
            }

            @Test
            @DisplayName("create persists and commits")
            void create() throws Exception {
                when(mockQuery.getResultList()).thenReturn(new ArrayList<>());
                SubmittedDocument entity = new SubmittedDocument();
                entity.setFkIdProcedure(new Procedure(1));
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy finds and removes")
            void destroy() throws Exception {
                SubmittedDocument entity = new SubmittedDocument();
                when(mockEm.getReference(SubmittedDocument.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("EscrituraJpaController")
        class DeedTest {
            private DeedJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(DeedJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("DeedJpaController"); }

            @Test
            @DisplayName("findEscrituraEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Deed.class)));
                assertThat(controller.findDeedEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findEscritura")
            void findById() {
                when(mockEm.find(Deed.class, 3)).thenReturn(mock(Deed.class));
                assertThat(controller.findDeed(3)).isNotNull();
            }

            @Test
            @DisplayName("getEscrituraCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(15L);
                assertThat(controller.getDeedCount()).isEqualTo(15);
            }

            @Test
            @DisplayName("create persists and commits")
            void create() throws Exception {
                Deed entity = new Deed();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy finds and removes")
            void destroy() throws Exception {
                Deed entity = new Deed();
                entity.setTestimonyList(new ArrayList<>());
                entity.setFolioList(new ArrayList<>());
                entity.setProcedureList(new ArrayList<>());
                when(mockEm.getReference(Deed.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("EstadoDeGestionJpaController")
        class ManagementStatusTest {
            private ManagementStatusJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(ManagementStatusJpaController.class);
            }

            @Test
            @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("ManagementStatusJpaController"); }

            @Test
            @DisplayName("findEstadoDeGestionEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(ManagementStatus.class)));
                assertThat(controller.findManagementStatusEntities()).hasSize(1);
            }

            @Test
            @DisplayName("findEstadoDeGestion")
            void findById() {
                when(mockEm.find(ManagementStatus.class, 2)).thenReturn(mock(ManagementStatus.class));
                assertThat(controller.findManagementStatus(2)).isNotNull();
            }

            @Test
            @DisplayName("getEstadoDeGestionCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(5L);
                assertThat(controller.getManagementStatusCount()).isEqualTo(5);
            }

            @Test
            @DisplayName("create persists")
            void create() throws Exception {
                ManagementStatus entity = new ManagementStatus();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test
            @DisplayName("destroy removes")
            void destroy() throws Exception {
                ManagementStatus entity = new ManagementStatus();
                entity.setHistoryList(new java.util.HashSet<>());
                when(mockEm.getReference(ManagementStatus.class, 1)).thenReturn(entity);
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
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("FolioJpaController"); }

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
                when(mockEm.getReference(Folio.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("GestionDeEscrituraJpaController")
        class DeedManagementTest {
            private DeedManagementJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(DeedManagementJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("DeedManagementJpaController"); }

            @Test @DisplayName("findGestionDeEscrituraEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(DeedManagement.class)));
                assertThat(controller.findDeedManagementEntities()).hasSize(1);
            }

            @Test @DisplayName("findGestionDeEscritura")
            void findById() {
                when(mockEm.find(DeedManagement.class, 7)).thenReturn(mock(DeedManagement.class));
                assertThat(controller.findDeedManagement(7)).isNotNull();
            }

            @Test @DisplayName("getGestionDeEscrituraCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(2L);
                assertThat(controller.getDeedManagementCount()).isEqualTo(2);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                DeedManagement entity = new DeedManagement();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                DeedManagement entity = new DeedManagement();
                when(mockEm.getReference(DeedManagement.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("HistorialJpaController")
        class HistoryTest {
            private HistoryJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(HistoryJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("HistoryJpaController"); }

            @Test @DisplayName("findHistorialEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(History.class)));
                assertThat(controller.findHistoryEntities()).hasSize(1);
            }

            @Test @DisplayName("findHistorial")
            void findById() {
                when(mockEm.find(History.class, 2)).thenReturn(mock(History.class));
                assertThat(controller.findHistory(2)).isNotNull();
            }

            @Test @DisplayName("getHistorialCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(10L);
                assertThat(controller.getHistoryCount()).isEqualTo(10);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                History entity = new History();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                History entity = new History();
                when(mockEm.getReference(History.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("InmuebleJpaController")
        class PropertyTest {
            private PropertyJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PropertyJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("PropertyJpaController"); }

            @Test @DisplayName("findInmuebleEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Property.class)));
                assertThat(controller.findPropertyEntities()).hasSize(1);
            }

            @Test @DisplayName("findInmueble")
            void findById() {
                when(mockEm.find(Property.class, 3)).thenReturn(mock(Property.class));
                assertThat(controller.findProperty(3)).isNotNull();
            }

            @Test @DisplayName("getInmuebleCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(6L);
                assertThat(controller.getPropertyCount()).isEqualTo(6);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Property entity = new Property();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Property entity = new Property();
                when(mockEm.getReference(Property.class, 1)).thenReturn(entity);
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
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("ItemJpaController"); }

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
                when(mockEm.getReference(Item.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("MovimientoTestimonioJpaController")
        class TestimonyMovementTest {
            private TestimonyMovementJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TestimonyMovementJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("TestimonyMovementJpaController"); }

            @Test @DisplayName("findMovimientoTestimonioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(TestimonyMovement.class)));
                assertThat(controller.findTestimonyMovementEntities()).hasSize(1);
            }

            @Test @DisplayName("findMovimientoTestimonio")
            void findById() {
                when(mockEm.find(TestimonyMovement.class, 5)).thenReturn(mock(TestimonyMovement.class));
                assertThat(controller.findTestimonyMovement(5)).isNotNull();
            }

            @Test @DisplayName("getMovimientoTestimonioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(9L);
                assertThat(controller.getTestimonyMovementCount()).isEqualTo(9);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                TestimonyMovement entity = new TestimonyMovement();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                TestimonyMovement entity = new TestimonyMovement();
                when(mockEm.getReference(TestimonyMovement.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PagoJpaController")
        class PaymentTest {
            private PaymentJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(PaymentJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("PaymentJpaController"); }

            @Test @DisplayName("findPagoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Payment.class)));
                assertThat(controller.findPaymentEntities()).hasSize(1);
            }

            @Test @DisplayName("findPago")
            void findById() {
                when(mockEm.find(Payment.class, 6)).thenReturn(mock(Payment.class));
                assertThat(controller.findPayment(6)).isNotNull();
            }

            @Test @DisplayName("getPagoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(11L);
                assertThat(controller.getPaymentCount()).isEqualTo(11);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Payment entity = new Payment();
                entity.setIdPayment(1);
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Payment entity = new Payment();
                when(mockEm.getReference(Payment.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PlantillaPresupuestoJpaController")
        class BudgetTemplateTest {
            private BudgetTemplateJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(BudgetTemplateJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("BudgetTemplateJpaController"); }

            @Test @DisplayName("findPlantillaPresupuestoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(BudgetTemplate.class)));
                assertThat(controller.findBudgetTemplateEntities()).hasSize(1);
            }

            @Test @DisplayName("findPlantillaPresupuesto")
            void findById() {
                BudgetTemplatePK id = new BudgetTemplatePK(2, 2);
                when(mockEm.find(BudgetTemplate.class, id)).thenReturn(mock(BudgetTemplate.class));
                assertThat(controller.findBudgetTemplate(id)).isNotNull();
            }

            @Test @DisplayName("getPlantillaPresupuestoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(8L);
                assertThat(controller.getBudgetTemplateCount()).isEqualTo(8);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                BudgetTemplate entity = new BudgetTemplate();
                entity.setProcedureType(new ProcedureType(1));
                entity.setConcept(new Concept(1));
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                BudgetTemplatePK id = new BudgetTemplatePK(1, 1);
                BudgetTemplate entity = new BudgetTemplate();
                when(mockEm.getReference(BudgetTemplate.class, id)).thenReturn(entity);
                controller.destroy(id);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PlantillaTramiteJpaController")
        class ProcedureTemplateTest {
            private ProcedureTemplateJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(ProcedureTemplateJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("ProcedureTemplateJpaController"); }

            @Test @DisplayName("findPlantillaTramiteEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(ProcedureTemplate.class)));
                assertThat(controller.findProcedureTemplateEntities()).hasSize(1);
            }

            @Test @DisplayName("findPlantillaTramite")
            void findById() {
                ProcedureTemplatePK id = new ProcedureTemplatePK(4, 4);
                when(mockEm.find(ProcedureTemplate.class, id)).thenReturn(mock(ProcedureTemplate.class));
                assertThat(controller.findProcedureTemplate(id)).isNotNull();
            }

            @Test @DisplayName("getPlantillaTramiteCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(12L);
                assertThat(controller.getProcedureTemplateCount()).isEqualTo(12);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                ProcedureTemplate entity = new ProcedureTemplate();
                entity.setProcedureType(new ProcedureType(1));
                entity.setDocumentType(new DocumentType(1));
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                ProcedureTemplatePK id = new ProcedureTemplatePK(1, 1);
                ProcedureTemplate entity = new ProcedureTemplate();
                when(mockEm.getReference(ProcedureTemplate.class, id)).thenReturn(entity);
                controller.destroy(id);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("PresupuestoJpaController")
        class BudgetTest {
            private BudgetJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(BudgetJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("BudgetJpaController"); }

            @Test @DisplayName("findPresupuestoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Budget.class)));
                assertThat(controller.findBudgetEntities()).hasSize(1);
            }

            @Test @DisplayName("findPresupuesto")
            void findById() {
                when(mockEm.find(Budget.class, 9)).thenReturn(mock(Budget.class));
                assertThat(controller.findBudget(9)).isNotNull();
            }

            @Test @DisplayName("getPresupuestoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(20L);
                assertThat(controller.getBudgetCount()).isEqualTo(20);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Budget entity = new Budget();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Budget entity = new Budget();
                when(mockEm.getReference(Budget.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }

        }

        @Nested
        @DisplayName("SuplenciaJpaController")
        class SubstitutionTest {
            private SubstitutionJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(SubstitutionJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("SubstitutionJpaController"); }

            @Test @DisplayName("findSuplenciaEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Substitution.class)));
                assertThat(controller.findSubstitutionEntities()).hasSize(1);
            }

            @Test @DisplayName("findSuplencia")
            void findById() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Substitution.class)));
                assertThat(controller.findSubstitution(3)).isNotNull();
            }

            @Test @DisplayName("findSuplencia returns null when not found")
            void findByIdNotFound() {
                when(mockQuery.getResultList()).thenReturn(List.of());
                assertThat(controller.findSubstitution(999)).isNull();
            }

            @Test @DisplayName("getSuplenciaCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(1L);
                assertThat(controller.getSubstitutionCount()).isEqualTo(1);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Substitution entity = new Substitution();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Substitution entity = new Substitution();
                when(mockEm.find(Substitution.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TestimonioJpaController")
        class TestimonyTest {
            private TestimonyJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(TestimonyJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("TestimonyJpaController"); }

            @Test @DisplayName("findTestimonioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(Testimony.class)));
                assertThat(controller.findTestimonyEntities()).hasSize(1);
            }

            @Test @DisplayName("findTestimonio")
            void findById() {
                when(mockEm.find(Testimony.class, 7)).thenReturn(mock(Testimony.class));
                assertThat(controller.findTestimony(7)).isNotNull();
            }

            @Test @DisplayName("getTestimonioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(3L);
                assertThat(controller.getTestimonyCount()).isEqualTo(3);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                Testimony entity = new Testimony();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                Testimony entity = new Testimony();
                when(mockEm.getReference(Testimony.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeDocumentoJpaController")
        class DocumentTypeTest {
            private DocumentTypeJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(DocumentTypeJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("DocumentTypeJpaController"); }

            @Test @DisplayName("findTipoDeDocumentoEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(DocumentType.class)));
                assertThat(controller.findDocumentTypeEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeDocumento")
            void findById() {
                when(mockEm.find(DocumentType.class, 4)).thenReturn(mock(DocumentType.class));
                assertThat(controller.findDocumentType(4)).isNotNull();
            }

            @Test @DisplayName("getTipoDeDocumentoCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(16L);
                assertThat(controller.getDocumentTypeCount()).isEqualTo(16);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                DocumentType entity = new DocumentType();
                entity.setIdDocumentType(1);
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                DocumentType entity = new DocumentType();
                entity.setProcedureTemplateList(new ArrayList<>());
                when(mockEm.find(DocumentType.class, 1)).thenReturn(entity);
                when(mockEm.getReference(DocumentType.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeFolioJpaController")
        class FolioTypeTest {
            private FolioTypeJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(FolioTypeJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("FolioTypeJpaController"); }

            @Test @DisplayName("findTipoDeFolioEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(FolioType.class)));
                assertThat(controller.findFolioTypeEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeFolio")
            void findById() {
                when(mockEm.find(FolioType.class, 5)).thenReturn(mock(FolioType.class));
                assertThat(controller.findFolioType(5)).isNotNull();
            }

            @Test @DisplayName("getTipoDeFolioCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(2L);
                assertThat(controller.getFolioTypeCount()).isEqualTo(2);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                FolioType entity = new FolioType();
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                FolioType entity = new FolioType();
                entity.setFolioList(new ArrayList<>());
                when(mockEm.getReference(FolioType.class, 1)).thenReturn(entity);
                controller.destroy(1);
                verify(mockEm).remove(entity);
                verify(mockTx).commit();
            }
        }

        @Nested
        @DisplayName("TipoDeTramiteJpaController")
        class ProcedureTypeTest {
            private ProcedureTypeJpaController controller;

            @BeforeEach
            void setUp() {
                controller = createStandardController(ProcedureTypeJpaController.class);
            }

            @Test @DisplayName("getNombreJpa")
            void getNameJpa() { assertThat(controller.getNameJpa()).contains("ProcedureTypeJpaController"); }

            @Test @DisplayName("findTipoDeTramiteEntities")
            void findEntities() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(ProcedureType.class)));
                assertThat(controller.findProcedureTypeEntities()).hasSize(1);
            }

            @Test @DisplayName("findTipoDeTramite")
            void findById() {
                when(mockQuery.getResultList()).thenReturn(List.of(mock(ProcedureType.class)));
                assertThat(controller.findProcedureType(6)).isNotNull();
            }

            @Test @DisplayName("getTipoDeTramiteCount")
            void getCount() {
                when(mockQuery.getSingleResult()).thenReturn(7L);
                assertThat(controller.getProcedureTypeCount()).isEqualTo(7);
            }

            @Test @DisplayName("create persists")
            void create() throws Exception {
                ProcedureType entity = new ProcedureType();
                entity.setIdProcedureType(1);
                controller.create(entity);
                verify(mockEm).persist(entity);
                verify(mockTx).commit();
            }

            @Test @DisplayName("destroy removes")
            void destroy() throws Exception {
                ProcedureType entity = new ProcedureType();
                entity.setBudgetTemplateList(new ArrayList<>());
                entity.setProcedureTemplateList(new ArrayList<>());
                entity.setProcedureList(new ArrayList<>());
                when(mockEm.find(ProcedureType.class, 1)).thenReturn(entity);
                when(mockEm.getReference(ProcedureType.class, 1)).thenReturn(entity);
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
    class IdentificationTypeSingletonTest {

        private void primeSingleton() {
            try {
                IdentificationTypeJpaController instance = new IdentificationTypeJpaController(mockUtx, mockEmf);
                java.lang.reflect.Field instanciaField =
                        IdentificationTypeJpaController.class.getDeclaredField("instancia");
                instanciaField.setAccessible(true);
                instanciaField.set(null, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        @DisplayName("getNombreJpa via singleton pattern")
        void getNameJpa() {
            primeSingleton();

            IdentificationTypeJpaController controller = IdentificationTypeJpaController.getInstancia();
            assertThat(controller.getNameJpa()).contains("IdentificationTypeJpaController");
        }

        @Test
        @DisplayName("getInstance returns singleton")
        void getInstanceSingleton() {
            primeSingleton();

            IdentificationTypeJpaController first = IdentificationTypeJpaController.getInstancia();
            IdentificationTypeJpaController second = IdentificationTypeJpaController.getInstancia();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("create persists and commits")
        void create() throws Exception {
            primeSingleton();

            IdentificationTypeJpaController controller = IdentificationTypeJpaController.getInstancia();
            IdentificationType entity = new IdentificationType();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("findTipoIdentificacionEntities")
        void findEntities() {
            primeSingleton();

            when(mockQuery.getResultList()).thenReturn(List.of(mock(IdentificationType.class)));
            IdentificationTypeJpaController controller = IdentificationTypeJpaController.getInstancia();
            assertThat(controller.findIdentificationTypeEntities()).hasSize(1);
        }

        @Test
        @DisplayName("getTipoIdentificacionCount")
        void getCount() {
            primeSingleton();

            when(mockQuery.getSingleResult()).thenReturn(5L);
            IdentificationTypeJpaController controller = IdentificationTypeJpaController.getInstancia();
            assertThat(controller.getIdentificationTypeCount()).isEqualTo(5);
        }
    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  PERSONA — singleton with private constructor, has extra query methods       ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("PersonJpaController (singleton)")
    class PersonSingletonTest {
        private PersonJpaController controller;

        @BeforeEach
        void setUp() throws Exception {
            // PersonJpaController has a private constructor — use reflection
            controller = createWithReflection(PersonJpaController.class, mockUtx, mockEmf);
        }

        @Test
        @DisplayName("getNombreJpa")
        void getNameJpa() { assertThat(controller.getNameJpa()).contains("PersonJpaController"); }

        @Test
        @DisplayName("findPersonaEntities")
        void findEntities() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Person.class)));
            assertThat(controller.findPersonEntities()).hasSize(1);
        }

        @Test
        @DisplayName("findPersonaEntities paginated")
        void findEntitiesPaginated() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Person.class)));
            assertThat(controller.findPersonEntities(20, 0)).hasSize(1);
            verify(mockQuery).setMaxResults(20);
            verify(mockQuery).setFirstResult(0);
        }

        @Test
        @DisplayName("findPersona")
        void findById() {
            when(mockEm.find(Person.class, 10)).thenReturn(mock(Person.class));
            assertThat(controller.findPerson(10)).isNotNull();
        }

        @Test
        @DisplayName("getPersonaCount")
        void getCount() {
            jakarta.persistence.criteria.CriteriaBuilder mockCb =
                    mock(jakarta.persistence.criteria.CriteriaBuilder.class);
            jakarta.persistence.criteria.CriteriaQuery mockCq =
                    mock(jakarta.persistence.criteria.CriteriaQuery.class);
            jakarta.persistence.criteria.Root mockRoot = mock(jakarta.persistence.criteria.Root.class);
            jakarta.persistence.TypedQuery mockTypedQuery = mock(jakarta.persistence.TypedQuery.class);
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery()).thenReturn(mockCq);
            when(mockCq.from(Person.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockCq)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getSingleResult()).thenReturn(50L);
            assertThat(controller.getPersonCount()).isEqualTo(50);
        }

        @Test
        @DisplayName("create persists")
        void create() throws Exception {
            Person entity = new Person();
            entity.setPersonId(1);
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("destroy removes")
        void destroy() throws Exception {
            Person entity = new Person();
            entity.setPersonProcedureList(new ArrayList<>());
            entity.setBudgetList(new ArrayList<>());
            entity.setDeedManagementList(new ArrayList<>());
            entity.setFolioList(new ArrayList<>());
            entity.setSubstitutionList(new ArrayList<>());
            entity.setSubstitutionList1(new ArrayList<>());
            entity.setCopyList(new ArrayList<>());
            entity.setUserList(new ArrayList<>());
            when(mockEm.getReference(Person.class, 1)).thenReturn(entity);
            controller.destroy(1);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }

        @Test
        @DisplayName("findPersonaNombreApellido uses named query")
        void findNameLastName() {
            when(mockEm.createNamedQuery("Persona.findByPersonaNombreApellido")).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("nombre"), anyString())).thenReturn(mockQuery);
            when(mockQuery.setParameter(eq("apellido"), anyString())).thenReturn(mockQuery);
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Person.class)));

            DtoPerson dto = new DtoPerson();
            dto.setFirstName("Juan");
            dto.setLastName("Perez");
            List<Person> result = controller.findPersonNameLastName(dto);
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
    class ProcedureTest {
        private ProcedureJpaController controller;

        @BeforeEach
        void setUp() {
            controller = createStandardController(ProcedureJpaController.class);
        }

        @Test @DisplayName("getNombreJpa")
        void getNameJpa() { assertThat(controller.getNameJpa()).contains("ProcedureJpaController"); }

        @Test @DisplayName("findTramiteEntities")
        void findEntities() {
            when(mockQuery.getResultList()).thenReturn(List.of(mock(Procedure.class)));
            assertThat(controller.findProcedureEntities()).hasSize(1);
        }

        @Test @DisplayName("findTramite")
        void findById() {
            when(mockEm.find(Procedure.class, 1)).thenReturn(mock(Procedure.class));
            assertThat(controller.findProcedure(1)).isNotNull();
        }

        @Test @DisplayName("getTramiteCount")
        void getCount() {
            jakarta.persistence.criteria.CriteriaBuilder mockCb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
            jakarta.persistence.criteria.CriteriaQuery mockCq = mock(jakarta.persistence.criteria.CriteriaQuery.class);
            jakarta.persistence.criteria.Root mockRoot = mock(jakarta.persistence.criteria.Root.class);
            jakarta.persistence.TypedQuery mockTypedQuery = mock(jakarta.persistence.TypedQuery.class);
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery()).thenReturn(mockCq);
            when(mockCq.from(Procedure.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockCq)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getSingleResult()).thenReturn(25L);
            assertThat(controller.getProcedureCount()).isEqualTo(25);
        }

        @Test @DisplayName("create persists")
        void create() throws Exception {
            Procedure entity = new Procedure();
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test @DisplayName("destroy removes")
        void destroy() throws Exception {
            Procedure entity = new Procedure();
            entity.setSubmittedDocumentList(new ArrayList<>());
            entity.setPersonProcedureList(new ArrayList<>());
            when(mockEm.getReference(Procedure.class, 1)).thenReturn(entity);
            controller.destroy(1);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }

    }

    //╔══════════════════════════════════════════════════════════════════════════════╗
    //║  TRAMITESPERSONAS — special constructor: (EntityManagerFactory) only        ║
    //╚══════════════════════════════════════════════════════════════════════════════╝

    @Nested
    @DisplayName("TramitesPersonasJpaController (single-arg constructor)")
    class PersonProcedureTest {
        private PersonProcedureJpaController controller;

        @BeforeEach
        void setUp() {
            controller = new PersonProcedureJpaController(mockEmf);
        }

        @Test @DisplayName("getNombreJpa")
        void getNameJpa() { assertThat(controller.getNameJpa()).contains("PersonProcedureJpaController"); }

        @Test @DisplayName("getEntityManager")
        void getEntityManager() { assertThat(controller.getEntityManager()).isSameAs(mockEm); }

        @Test @DisplayName("findTramitesPersonasEntities")
        void findEntities() {
            jakarta.persistence.criteria.CriteriaBuilder mockCb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
            jakarta.persistence.criteria.CriteriaQuery mockCq = mock(jakarta.persistence.criteria.CriteriaQuery.class);
            jakarta.persistence.criteria.Root mockRoot = mock(jakarta.persistence.criteria.Root.class);
            jakarta.persistence.TypedQuery mockTypedQuery = mock(jakarta.persistence.TypedQuery.class);
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery()).thenReturn(mockCq);
            when(mockCq.from(PersonProcedure.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockCq)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getResultList()).thenReturn(List.of(mock(PersonProcedure.class)));
            assertThat(controller.findPersonProcedureEntities()).hasSize(1);
        }

        @Test @DisplayName("findTramitesPersonas")
        void findById() {
            PersonProcedurePK id = new PersonProcedurePK(2, 2);
            when(mockEm.find(PersonProcedure.class, id)).thenReturn(mock(PersonProcedure.class));
            assertThat(controller.findPersonProcedure(id)).isNotNull();
        }

        @Test @DisplayName("getTramitesPersonasCount")
        void getCount() {
            jakarta.persistence.criteria.CriteriaBuilder mockCb = mock(jakarta.persistence.criteria.CriteriaBuilder.class);
            jakarta.persistence.criteria.CriteriaQuery mockCq = mock(jakarta.persistence.criteria.CriteriaQuery.class);
            jakarta.persistence.criteria.Root mockRoot = mock(jakarta.persistence.criteria.Root.class);
            jakarta.persistence.TypedQuery mockTypedQuery = mock(jakarta.persistence.TypedQuery.class);
            when(mockEm.getCriteriaBuilder()).thenReturn(mockCb);
            when(mockCb.createQuery()).thenReturn(mockCq);
            when(mockCq.from(PersonProcedure.class)).thenReturn(mockRoot);
            when(mockEm.createQuery(mockCq)).thenReturn(mockTypedQuery);
            when(mockTypedQuery.getSingleResult()).thenReturn(8L);
            assertThat(controller.getPersonProcedureCount()).isEqualTo(8);
        }

        @Test @DisplayName("create persists")
        void create() throws Exception {
            PersonProcedure entity = new PersonProcedure();
            entity.setPerson(new Person(1));
            entity.setProcedure(new Procedure(1));
            controller.create(entity);
            verify(mockEm).persist(entity);
            verify(mockTx).commit();
        }

        @Test @DisplayName("destroy removes")
        void destroy() throws Exception {
            PersonProcedurePK id = new PersonProcedurePK(1, 1);
            PersonProcedure entity = new PersonProcedure();
            when(mockEm.getReference(PersonProcedure.class, id)).thenReturn(entity);
            controller.destroy(id);
            verify(mockEm).remove(entity);
            verify(mockTx).commit();
        }
    }
}
