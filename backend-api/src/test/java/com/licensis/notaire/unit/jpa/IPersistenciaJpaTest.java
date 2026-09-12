package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptJpaController;
import com.licensis.notaire.jpa.CopyJpaController;
import com.licensis.notaire.jpa.SubmittedDocumentJpaController;
import com.licensis.notaire.jpa.DeedJpaController;
import com.licensis.notaire.jpa.ManagementStatusJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.DeedManagementJpaController;
import com.licensis.notaire.jpa.HistoryJpaController;
import com.licensis.notaire.jpa.PropertyJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.TestimonyMovementJpaController;
import com.licensis.notaire.jpa.PaymentJpaController;
import com.licensis.notaire.jpa.BudgetTemplateJpaController;
import com.licensis.notaire.jpa.ProcedureTemplateJpaController;
import com.licensis.notaire.jpa.BudgetJpaController;
import com.licensis.notaire.jpa.PersonJpaController;
import com.licensis.notaire.jpa.AuditRecordJpaController;
import com.licensis.notaire.jpa.SubstitutionJpaController;
import com.licensis.notaire.jpa.TestimonyJpaController;
import com.licensis.notaire.jpa.DocumentTypeJpaController;
import com.licensis.notaire.jpa.FolioTypeJpaController;
import com.licensis.notaire.jpa.ProcedureTypeJpaController;
import com.licensis.notaire.jpa.IdentificationTypeJpaController;
import com.licensis.notaire.jpa.ProcedureJpaController;
import com.licensis.notaire.jpa.PersonProcedureJpaController;
import com.licensis.notaire.jpa.UserJpaController;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IPersistenciaJpa implementation tests")
@ExtendWith(MockitoExtension.class)
class IPersistenciaJpaTest {

    @Mock
    private UserTransaction utx;

    @Mock
    private EntityManagerFactory emf;

    /**
     * Helper to instantiate a JpaController that has a private constructor.
     */
    @SuppressWarnings("unchecked")
    private <T extends IPersistenciaJpa> T createWithReflection(Class<T> clazz, Object... args) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> target = null;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == args.length) {
                    target = c;
                    break;
                }
            }
            if (target == null) {
                throw new RuntimeException("No constructor with " + args.length + " params for " + clazz.getSimpleName());
            }
            target.setAccessible(true);
            return (T) target.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName() + " via reflection", e);
        }
    }

    @Test
    @DisplayName("ConceptoJpaController.getNombreJpa should return its class name")
    void conceptJpaControllerGetNameJpa() {
        ConceptJpaController ctrl = new ConceptJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ConceptJpaController.class.getName());
    }

    @Test
    @DisplayName("CopiaJpaController.getNombreJpa should return its class name")
    void copyJpaControllerGetNameJpa() {
        CopyJpaController ctrl = new CopyJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(CopyJpaController.class.getName());
    }

    @Test
    @DisplayName("DocumentoPresentadoJpaController.getNombreJpa should return its class name")
    void submittedDocumentJpaControllerGetNameJpa() {
        SubmittedDocumentJpaController ctrl = new SubmittedDocumentJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(SubmittedDocumentJpaController.class.getName());
    }

    @Test
    @DisplayName("EscrituraJpaController.getNombreJpa should return its class name")
    void deedJpaControllerGetNameJpa() {
        DeedJpaController ctrl = new DeedJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(DeedJpaController.class.getName());
    }

    @Test
    @DisplayName("EstadoDeGestionJpaController.getNombreJpa should return its class name")
    void managementStatusJpaControllerGetNameJpa() {
        ManagementStatusJpaController ctrl = new ManagementStatusJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ManagementStatusJpaController.class.getName());
    }

    @Test
    @DisplayName("FolioJpaController.getNombreJpa should return its class name")
    void folioJpaControllerGetNameJpa() {
        FolioJpaController ctrl = new FolioJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(FolioJpaController.class.getName());
    }

    @Test
    @DisplayName("GestionDeEscrituraJpaController.getNombreJpa should return its class name")
    void deedManagementJpaControllerGetNameJpa() {
        DeedManagementJpaController ctrl = new DeedManagementJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(DeedManagementJpaController.class.getName());
    }

    @Test
    @DisplayName("HistorialJpaController.getNombreJpa should return its class name")
    void historyJpaControllerGetNameJpa() {
        HistoryJpaController ctrl = new HistoryJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(HistoryJpaController.class.getName());
    }

    @Test
    @DisplayName("InmuebleJpaController.getNombreJpa should return its class name")
    void propertyJpaControllerGetNameJpa() {
        PropertyJpaController ctrl = new PropertyJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(PropertyJpaController.class.getName());
    }

    @Test
    @DisplayName("ItemJpaController.getNombreJpa should return its class name")
    void itemJpaControllerGetNameJpa() {
        ItemJpaController ctrl = new ItemJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ItemJpaController.class.getName());
    }

    @Test
    @DisplayName("MovimientoTestimonioJpaController.getNombreJpa should return its class name")
    void testimonyMovementJpaControllerGetNameJpa() {
        TestimonyMovementJpaController ctrl = new TestimonyMovementJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(TestimonyMovementJpaController.class.getName());
    }

    @Test
    @DisplayName("PagoJpaController.getNombreJpa should return its class name")
    void paymentJpaControllerGetNameJpa() {
        PaymentJpaController ctrl = new PaymentJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(PaymentJpaController.class.getName());
    }

    @Test
    @DisplayName("PersonaJpaController.getNombreJpa should return its class name via reflection")
    void personJpaControllerGetNameJpa() {
        PersonJpaController ctrl = createWithReflection(PersonJpaController.class, utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(PersonJpaController.class.getName());
    }

    @Test
    @DisplayName("PlantillaPresupuestoJpaController.getNombreJpa should return its class name")
    void budgetTemplateJpaControllerGetNameJpa() {
        BudgetTemplateJpaController ctrl = new BudgetTemplateJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(BudgetTemplateJpaController.class.getName());
    }

    @Test
    @DisplayName("PlantillaTramiteJpaController.getNombreJpa should return its class name")
    void procedureTemplateJpaControllerGetNameJpa() {
        ProcedureTemplateJpaController ctrl = new ProcedureTemplateJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ProcedureTemplateJpaController.class.getName());
    }

    @Test
    @DisplayName("PresupuestoJpaController.getNombreJpa should return its class name")
    void budgetJpaControllerGetNameJpa() {
        BudgetJpaController ctrl = new BudgetJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(BudgetJpaController.class.getName());
    }

    @Test
    @DisplayName("RegistroAuditoriaJpaController.getNombreJpa should return its class name via reflection")
    void auditRecordJpaControllerGetNameJpa() {
        AuditRecordJpaController ctrl = createWithReflection(AuditRecordJpaController.class, utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(AuditRecordJpaController.class.getName());
    }

    @Test
    @DisplayName("SuplenciaJpaController.getNombreJpa should return its class name")
    void substitutionJpaControllerGetNameJpa() {
        SubstitutionJpaController ctrl = new SubstitutionJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(SubstitutionJpaController.class.getName());
    }

    @Test
    @DisplayName("TestimonioJpaController.getNombreJpa should return its class name")
    void testimonyJpaControllerGetNameJpa() {
        TestimonyJpaController ctrl = new TestimonyJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(TestimonyJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeDocumentoJpaController.getNombreJpa should return its class name")
    void documentTypeJpaControllerGetNameJpa() {
        DocumentTypeJpaController ctrl = new DocumentTypeJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(DocumentTypeJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeFolioJpaController.getNombreJpa should return its class name")
    void folioTypeJpaControllerGetNameJpa() {
        FolioTypeJpaController ctrl = new FolioTypeJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(FolioTypeJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeTramiteJpaController.getNombreJpa should return its class name")
    void procedureTypeJpaControllerGetNameJpa() {
        ProcedureTypeJpaController ctrl = new ProcedureTypeJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ProcedureTypeJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoIdentificacionJpaController.getNombreJpa should return its class name")
    void identificationTypeJpaControllerGetNameJpa() {
        IdentificationTypeJpaController ctrl = new IdentificationTypeJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(IdentificationTypeJpaController.class.getName());
    }

    @Test
    @DisplayName("TramitesPersonasJpaController.getNombreJpa should return its class name")
    void personProcedureJpaControllerGetNameJpa() {
        PersonProcedureJpaController ctrl = new PersonProcedureJpaController(emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(PersonProcedureJpaController.class.getName());
    }

    @Test
    @DisplayName("TramiteJpaController.getNombreJpa should return its class name")
    void procedureJpaControllerGetNameJpa() {
        ProcedureJpaController ctrl = new ProcedureJpaController(utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(ProcedureJpaController.class.getName());
    }

    @Test
    @DisplayName("UsuarioJpaController.getNombreJpa should return its class name via reflection")
    void userJpaControllerGetNameJpa() {
        UserJpaController ctrl = createWithReflection(UserJpaController.class, utx, emf);
        assertThat(ctrl.getNameJpa()).isEqualTo(UserJpaController.class.getName());
    }
}
