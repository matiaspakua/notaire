package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptoJpaController;
import com.licensis.notaire.jpa.EstadoDeGestionJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.PersonaJpaController;
import com.licensis.notaire.jpa.RegistroAuditoriaJpaController;
import com.licensis.notaire.jpa.SuplenciaJpaController;
import com.licensis.notaire.jpa.TipoDeFolioJpaController;
import com.licensis.notaire.jpa.TipoDeDocumentoJpaController;
import com.licensis.notaire.jpa.TipoDeTramiteJpaController;
import com.licensis.notaire.jpa.PlantillaTramiteJpaController;
import com.licensis.notaire.jpa.TipoIdentificacionJpaController;
import com.licensis.notaire.jpa.HistorialJpaController;
import com.licensis.notaire.jpa.TramiteJpaController;
import com.licensis.notaire.jpa.GestionDeEscrituraJpaController;
import com.licensis.notaire.jpa.PresupuestoJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.InmuebleJpaController;
import com.licensis.notaire.jpa.PlantillaPresupuestoJpaController;
import com.licensis.notaire.jpa.TramitesPersonasJpaController;
import com.licensis.notaire.jpa.PagoJpaController;
import com.licensis.notaire.jpa.EscrituraJpaController;
import com.licensis.notaire.jpa.CopiaJpaController;
import com.licensis.notaire.jpa.TestimonioJpaController;
import com.licensis.notaire.jpa.MovimientoTestimonioJpaController;
import com.licensis.notaire.jpa.DocumentoPresentadoJpaController;
import com.licensis.notaire.jpa.UsuarioJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.service.AdministradorJpa;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("AdministradorJpa unit tests")
class AdministradorJpaTest {

    private EntityManagerFactory mockEmf;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to reset the singleton state
        java.lang.reflect.Field instanciaField = AdministradorJpa.class.getDeclaredField("instancia");
        instanciaField.setAccessible(true);
        instanciaField.set(null, null);

        java.lang.reflect.Field emfField = AdministradorJpa.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field milistaJpasField = AdministradorJpa.class.getDeclaredField("milistaJpas");
        milistaJpasField.setAccessible(true);
        milistaJpasField.set(null, null);

        mockEmf = mock(EntityManagerFactory.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        java.lang.reflect.Field instanciaField = AdministradorJpa.class.getDeclaredField("instancia");
        instanciaField.setAccessible(true);
        instanciaField.set(null, null);

        java.lang.reflect.Field emfField = AdministradorJpa.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field milistaJpasField = AdministradorJpa.class.getDeclaredField("milistaJpas");
        milistaJpasField.setAccessible(true);
        milistaJpasField.set(null, null);
    }

    @Nested
    @DisplayName("getInstancia")
    class GetInstancia {

        @Test
        @DisplayName("should return singleton instance")
        void shouldReturnSingleton() {
            AdministradorJpa first = AdministradorJpa.getInstancia();
            AdministradorJpa second = AdministradorJpa.getInstancia();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("setEmf")
    class SetEmf {

        @Test
        @DisplayName("should set EMF and deregister when instancia is null")
        void shouldSetEmfWithoutLoadingList() {
            AdministradorJpa.setEmf(mockEmf);
            assertThat(AdministradorJpa.getEmf()).isSameAs(mockEmf);
            // milistaJpas should be null since instancia is null (no call to cargarListaJpas)
            // Note: after getInstancia(), setEmf will trigger cargarListaJpas()
        }

        @Test
        @DisplayName("should reload JPA list when instancia already exists")
        void shouldReloadListWhenInstanciaExists() {
            // First get instance (instancia created, but no emf yet)
            AdministradorJpa.getInstancia();

            // Now set EMF - should trigger cargarListaJpas via the if (instancia != null) block
            AdministradorJpa.setEmf(mockEmf);

            Collection<IPersistenciaJpa> list = AdministradorJpa.getMilistaJpas();
            assertThat(list).isNotNull();
            assertThat(list).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("cargarListaJpas")
    class CargarListaJpas {

        @BeforeEach
        void setUp() {
            AdministradorJpa.getInstancia();
        }

        @Test
        @DisplayName("should populate milistaJpas with all controllers")
        void shouldPopulateAllControllers() {
            // This test verifies that calling setEmf after instancia triggers cargarListaJpas
            // which adds all 26 controllers to the list
            AdministradorJpa.setEmf(mockEmf);

            Collection<IPersistenciaJpa> list = AdministradorJpa.getMilistaJpas();
            assertThat(list).isNotNull();
            assertThat(list).hasSizeGreaterThan(20);

            // Verify a few specific controller types exist
            assertThat(list).anyMatch(jpa ->
                    jpa.getNombreJpa().contains("ConceptoJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNombreJpa().contains("PersonaJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNombreJpa().contains("UsuarioJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNombreJpa().contains("RegistroAuditoriaJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNombreJpa().contains("TramitesPersonasJpaController"));
        }
    }

    @Nested
    @DisplayName("obtenerJpa")
    class ObtenerJpa {

        @BeforeEach
        void setUp() {
            AdministradorJpa.getInstancia();
            AdministradorJpa.setEmf(mockEmf);
        }

        @Test
        @DisplayName("should return matching JPA by class name")
        void shouldFindMatchingJpa() throws Exception {
            IPersistenciaJpa jpa = AdministradorJpa.getInstancia()
                    .obtenerJpa("ConceptoJpaController");
            assertThat(jpa).isNotNull();
            assertThat(jpa).isInstanceOf(ConceptoJpaController.class);
        }

        @Test
        @DisplayName("should throw NonexistentJpaException when not found")
        void shouldThrowWhenNotFound() {
            assertThatThrownBy(() ->
                    AdministradorJpa.getInstancia().obtenerJpa("NonExistentJpa"))
                    .isInstanceOf(NonexistentJpaException.class)
                    .hasMessage("El JPA indicado no existe.");
        }
    }

    @Nested
    @DisplayName("getMilistaJpas / setMilistaJpas")
    class GetSetList {

        @Test
        @DisplayName("should allow direct list manipulation")
        void shouldAllowDirectListAccess() {
            java.util.ArrayList<IPersistenciaJpa> customList = new java.util.ArrayList<>();
            AdministradorJpa.setMilistaJpas(customList);
            assertThat(AdministradorJpa.getMilistaJpas()).isSameAs(customList);
        }
    }

    @Nested
    @DisplayName("getEmf")
    class GetEmf {

        @Test
        @DisplayName("should return null when not set")
        void shouldReturnNullWhenNotSet() {
            assertThat(AdministradorJpa.getEmf()).isNull();
        }

        @Test
        @DisplayName("should return set EMF")
        void shouldReturnSetEmf() {
            AdministradorJpa.setEmf(mockEmf);
            assertThat(AdministradorJpa.getEmf()).isSameAs(mockEmf);
        }
    }
}
