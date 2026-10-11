package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.ConceptJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.service.JpaAdministrator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@DisplayName("JpaAdministrator unit tests")
class AdministradorJpaTest {

    private EntityManagerFactory mockEmf;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to reset the singleton state
        java.lang.reflect.Field instanciaField = JpaAdministrator.class.getDeclaredField("instancia");
        instanciaField.setAccessible(true);
        instanciaField.set(null, null);

        java.lang.reflect.Field emfField = JpaAdministrator.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field milistaJpasField = JpaAdministrator.class.getDeclaredField("milistaJpas");
        milistaJpasField.setAccessible(true);
        milistaJpasField.set(null, null);

        mockEmf = mock(EntityManagerFactory.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        java.lang.reflect.Field instanciaField = JpaAdministrator.class.getDeclaredField("instancia");
        instanciaField.setAccessible(true);
        instanciaField.set(null, null);

        java.lang.reflect.Field emfField = JpaAdministrator.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field milistaJpasField = JpaAdministrator.class.getDeclaredField("milistaJpas");
        milistaJpasField.setAccessible(true);
        milistaJpasField.set(null, null);
    }

    @Nested
    @DisplayName("getInstancia")
    class GetInstancia {

        @Test
        @DisplayName("should return singleton instance")
        void shouldReturnSingleton() {
            JpaAdministrator first = JpaAdministrator.getInstancia();
            JpaAdministrator second = JpaAdministrator.getInstancia();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("setEmf")
    class SetEmf {

        @Test
        @DisplayName("should set EMF and deregister when instancia is null")
        void shouldSetEmfWithoutLoadingList() {
            JpaAdministrator.setEmf(mockEmf);
            assertThat(JpaAdministrator.getEmf()).isSameAs(mockEmf);
            // milistaJpas should be null since instancia is null (no call to loadJpaList)
            // Note: after getInstancia(), setEmf will trigger loadJpaList()
        }

        @Test
        @DisplayName("should reload JPA list when instancia already exists")
        void shouldReloadListWhenInstanciaExists() {
            // First get instance (instancia created, but no emf yet)
            JpaAdministrator.getInstancia();

            // Now set EMF - should trigger loadJpaList via the if (instancia != null) block
            JpaAdministrator.setEmf(mockEmf);

            Collection<JpaPersistence> list = JpaAdministrator.getMilistaJpas();
            assertThat(list).isNotNull();
            assertThat(list).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("loadJpaList")
    class CargarListaJpas {

        @BeforeEach
        void setUp() {
            JpaAdministrator.getInstancia();
        }

        @Test
        @DisplayName("should populate milistaJpas with all controllers")
        void shouldPopulateAllControllers() {
            // This test verifies that calling setEmf after instancia triggers loadJpaList
            // which adds all 26 controllers to the list
            JpaAdministrator.setEmf(mockEmf);

            Collection<JpaPersistence> list = JpaAdministrator.getMilistaJpas();
            assertThat(list).isNotNull();
            assertThat(list).hasSizeGreaterThan(20);

            // Verify a few specific controller types exist
            assertThat(list).anyMatch(jpa ->
                    jpa.getNameJpa().contains("ConceptJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNameJpa().contains("PersonJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNameJpa().contains("UserJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNameJpa().contains("AuditRecordJpaController"));
            assertThat(list).anyMatch(jpa ->
                    jpa.getNameJpa().contains("PersonProcedureJpaController"));
        }
    }

    @Nested
    @DisplayName("getJpa")
    class ObtenerJpa {

        @BeforeEach
        void setUp() {
            JpaAdministrator.getInstancia();
            JpaAdministrator.setEmf(mockEmf);
        }

        @Test
        @DisplayName("should return matching JPA by class name")
        void shouldFindMatchingJpa() throws Exception {
            JpaPersistence jpa = JpaAdministrator.getInstancia()
                    .getJpa("ConceptJpaController");
            assertThat(jpa).isNotNull();
            assertThat(jpa).isInstanceOf(ConceptJpaController.class);
        }

        @Test
        @DisplayName("should throw NonexistentJpaException when not found")
        void shouldThrowWhenNotFound() {
            assertThatThrownBy(() ->
                    JpaAdministrator.getInstancia().getJpa("NonExistentJpa"))
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
            java.util.ArrayList<JpaPersistence> customList = new java.util.ArrayList<>();
            JpaAdministrator.setMilistaJpas(customList);
            assertThat(JpaAdministrator.getMilistaJpas()).isSameAs(customList);
        }
    }

    @Nested
    @DisplayName("getEmf")
    class GetEmf {

        @Test
        @DisplayName("should return null when not set")
        void shouldReturnNullWhenNotSet() {
            assertThat(JpaAdministrator.getEmf()).isNull();
        }

        @Test
        @DisplayName("should return set EMF")
        void shouldReturnSetEmf() {
            JpaAdministrator.setEmf(mockEmf);
            assertThat(JpaAdministrator.getEmf()).isSameAs(mockEmf);
        }
    }
}
