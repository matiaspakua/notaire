package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.jpa.CopiaJpaController;
import com.licensis.notaire.jpa.DocumentoPresentadoJpaController;
import com.licensis.notaire.jpa.EscrituraJpaController;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.jpa.PagoJpaController;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.PlantillaTramiteJpaController;
import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.jpa.TestimonioJpaController;
import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.jpa.TipoDeTramiteJpaController;
import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.jpa.TramiteJpaController;
import com.licensis.notaire.jpa.TramitesPersonasJpaController;
import com.licensis.notaire.jpa.UsuarioJpaController;
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
    void conceptoJpaControllerGetNombreJpa() {
        ConceptoJpaController ctrl = new ConceptoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(ConceptoJpaController.class.getName());
    }

    @Test
    @DisplayName("CopiaJpaController.getNombreJpa should return its class name")
    void copiaJpaControllerGetNombreJpa() {
        CopiaJpaController ctrl = new CopiaJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(CopiaJpaController.class.getName());
    }

    @Test
    @DisplayName("DocumentoPresentadoJpaController.getNombreJpa should return its class name")
    void documentoPresentadoJpaControllerGetNombreJpa() {
        DocumentoPresentadoJpaController ctrl = new DocumentoPresentadoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(DocumentoPresentadoJpaController.class.getName());
    }

    @Test
    @DisplayName("EscrituraJpaController.getNombreJpa should return its class name")
    void escrituraJpaControllerGetNombreJpa() {
        EscrituraJpaController ctrl = new EscrituraJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(EscrituraJpaController.class.getName());
    }

    @Test
    @DisplayName("EstadoDeGestionJpaController.getNombreJpa should return its class name")
    void estadoDeGestionJpaControllerGetNombreJpa() {
        EstadoDeGestionJpaController ctrl = new EstadoDeGestionJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(EstadoDeGestionJpaController.class.getName());
    }

    @Test
    @DisplayName("FolioJpaController.getNombreJpa should return its class name")
    void folioJpaControllerGetNombreJpa() {
        FolioJpaController ctrl = new FolioJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(FolioJpaController.class.getName());
    }

    @Test
    @DisplayName("GestionDeEscrituraJpaController.getNombreJpa should return its class name")
    void gestionDeEscrituraJpaControllerGetNombreJpa() {
        GestionDeEscrituraJpaController ctrl = new GestionDeEscrituraJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(GestionDeEscrituraJpaController.class.getName());
    }

    @Test
    @DisplayName("HistorialJpaController.getNombreJpa should return its class name")
    void historialJpaControllerGetNombreJpa() {
        HistorialJpaController ctrl = new HistorialJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(HistorialJpaController.class.getName());
    }

    @Test
    @DisplayName("InmuebleJpaController.getNombreJpa should return its class name")
    void inmuebleJpaControllerGetNombreJpa() {
        InmuebleJpaController ctrl = new InmuebleJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(InmuebleJpaController.class.getName());
    }

    @Test
    @DisplayName("ItemJpaController.getNombreJpa should return its class name")
    void itemJpaControllerGetNombreJpa() {
        ItemJpaController ctrl = new ItemJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(ItemJpaController.class.getName());
    }

    @Test
    @DisplayName("MovimientoTestimonioJpaController.getNombreJpa should return its class name")
    void movimientoTestimonioJpaControllerGetNombreJpa() {
        MovimientoTestimonioJpaController ctrl = new MovimientoTestimonioJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(MovimientoTestimonioJpaController.class.getName());
    }

    @Test
    @DisplayName("PagoJpaController.getNombreJpa should return its class name")
    void pagoJpaControllerGetNombreJpa() {
        PagoJpaController ctrl = new PagoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(PagoJpaController.class.getName());
    }

    @Test
    @DisplayName("PersonaJpaController.getNombreJpa should return its class name via reflection")
    void personaJpaControllerGetNombreJpa() {
        PersonaJpaController ctrl = createWithReflection(PersonaJpaController.class, utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(PersonaJpaController.class.getName());
    }

    @Test
    @DisplayName("PlantillaPresupuestoJpaController.getNombreJpa should return its class name")
    void plantillaPresupuestoJpaControllerGetNombreJpa() {
        PlantillaPresupuestoJpaController ctrl = new PlantillaPresupuestoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(PlantillaPresupuestoJpaController.class.getName());
    }

    @Test
    @DisplayName("PlantillaTramiteJpaController.getNombreJpa should return its class name")
    void plantillaTramiteJpaControllerGetNombreJpa() {
        PlantillaTramiteJpaController ctrl = new PlantillaTramiteJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(PlantillaTramiteJpaController.class.getName());
    }

    @Test
    @DisplayName("PresupuestoJpaController.getNombreJpa should return its class name")
    void presupuestoJpaControllerGetNombreJpa() {
        PresupuestoJpaController ctrl = new PresupuestoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(PresupuestoJpaController.class.getName());
    }

    @Test
    @DisplayName("RegistroAuditoriaJpaController.getNombreJpa should return its class name via reflection")
    void registroAuditoriaJpaControllerGetNombreJpa() {
        RegistroAuditoriaJpaController ctrl = createWithReflection(RegistroAuditoriaJpaController.class, utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(RegistroAuditoriaJpaController.class.getName());
    }

    @Test
    @DisplayName("SuplenciaJpaController.getNombreJpa should return its class name")
    void suplenciaJpaControllerGetNombreJpa() {
        SuplenciaJpaController ctrl = new SuplenciaJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(SuplenciaJpaController.class.getName());
    }

    @Test
    @DisplayName("TestimonioJpaController.getNombreJpa should return its class name")
    void testimonioJpaControllerGetNombreJpa() {
        TestimonioJpaController ctrl = new TestimonioJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TestimonioJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeDocumentoJpaController.getNombreJpa should return its class name")
    void tipoDeDocumentoJpaControllerGetNombreJpa() {
        TipoDeDocumentoJpaController ctrl = new TipoDeDocumentoJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TipoDeDocumentoJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeFolioJpaController.getNombreJpa should return its class name")
    void tipoDeFolioJpaControllerGetNombreJpa() {
        TipoDeFolioJpaController ctrl = new TipoDeFolioJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TipoDeFolioJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoDeTramiteJpaController.getNombreJpa should return its class name")
    void tipoDeTramiteJpaControllerGetNombreJpa() {
        TipoDeTramiteJpaController ctrl = new TipoDeTramiteJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TipoDeTramiteJpaController.class.getName());
    }

    @Test
    @DisplayName("TipoIdentificacionJpaController.getNombreJpa should return its class name")
    void tipoIdentificacionJpaControllerGetNombreJpa() {
        TipoIdentificacionJpaController ctrl = new TipoIdentificacionJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TipoIdentificacionJpaController.class.getName());
    }

    @Test
    @DisplayName("TramitesPersonasJpaController.getNombreJpa should return its class name")
    void tramitesPersonasJpaControllerGetNombreJpa() {
        TramitesPersonasJpaController ctrl = new TramitesPersonasJpaController(emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TramitesPersonasJpaController.class.getName());
    }

    @Test
    @DisplayName("TramiteJpaController.getNombreJpa should return its class name")
    void tramiteJpaControllerGetNombreJpa() {
        TramiteJpaController ctrl = new TramiteJpaController(utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(TramiteJpaController.class.getName());
    }

    @Test
    @DisplayName("UsuarioJpaController.getNombreJpa should return its class name via reflection")
    void usuarioJpaControllerGetNombreJpa() {
        UsuarioJpaController ctrl = createWithReflection(UsuarioJpaController.class, utx, emf);
        assertThat(ctrl.getNombreJpa()).isEqualTo(UsuarioJpaController.class.getName());
    }
}
