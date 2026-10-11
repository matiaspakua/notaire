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
class JpaAdministratorTest {

    private EntityManagerFactory mockEmf;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to reset the singleton state
        java.lang.reflect.Field instanceField = JpaAdministrator.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        java.lang.reflect.Field emfField = JpaAdministrator.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field jpaListField = JpaAdministrator.class.getDeclaredField("jpaList");
        jpaListField.setAccessible(true);
        jpaListField.set(null, null);

        mockEmf = mock(EntityManagerFactory.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        java.lang.reflect.Field instanceField = JpaAdministrator.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        java.lang.reflect.Field emfField = JpaAdministrator.class.getDeclaredField("emf");
        emfField.setAccessible(true);
        emfField.set(null, null);

        java.lang.reflect.Field jpaListField = JpaAdministrator.class.getDeclaredField("jpaList");
        jpaListField.setAccessible(true);
        jpaListField.set(null, null);
    }

    @Nested
    @DisplayName("getInstance")
    class GetInstance {

        @Test
        @DisplayName("should return singleton instance")
        void shouldReturnSingleton() {
            JpaAdministrator first = JpaAdministrator.getInstance();
            JpaAdministrator second = JpaAdministrator.getInstance();
            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("setEmf")
    class SetEmf {

        @Test
        @DisplayName("should set EMF and deregister when instance is null")
        void shouldSetEmfWithoutLoadingList() {
            JpaAdministrator.setEmf(mockEmf);
            assertThat(JpaAdministrator.getEmf()).isSameAs(mockEmf);
            // jpaList should be null since instance is null (no call to loadJpaList)
            // Note: after getInstance(), setEmf will trigger loadJpaList()
        }

        @Test
        @DisplayName("should reload JPA list when instance already exists")
        void shouldReloadListWhenInstanceExists() {
            // First get instance (instance created, but no emf yet)
            JpaAdministrator.getInstance();

            // Now set EMF - should trigger loadJpaList via the if (instance != null) block
            JpaAdministrator.setEmf(mockEmf);

            Collection<JpaPersistence> list = JpaAdministrator.getJpaList();
            assertThat(list).isNotNull();
            assertThat(list).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("loadJpaList")
    class LoadJpaList {

        @BeforeEach
        void setUp() {
            JpaAdministrator.getInstance();
        }

        @Test
        @DisplayName("should populate jpaList with all controllers")
        void shouldPopulateAllControllers() {
            // This test verifies that calling setEmf after instance triggers loadJpaList
            // which adds all 26 controllers to the list
            JpaAdministrator.setEmf(mockEmf);

            Collection<JpaPersistence> list = JpaAdministrator.getJpaList();
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
    class GetJpa {

        @BeforeEach
        void setUp() {
            JpaAdministrator.getInstance();
            JpaAdministrator.setEmf(mockEmf);
        }

        @Test
        @DisplayName("should return matching JPA by class name")
        void shouldFindMatchingJpa() throws Exception {
            JpaPersistence jpa = JpaAdministrator.getInstance()
                    .getJpa("ConceptJpaController");
            assertThat(jpa).isNotNull();
            assertThat(jpa).isInstanceOf(ConceptJpaController.class);
        }

        @Test
        @DisplayName("should throw NonexistentJpaException when not found")
        void shouldThrowWhenNotFound() {
            assertThatThrownBy(() ->
                    JpaAdministrator.getInstance().getJpa("NonExistentJpa"))
                    .isInstanceOf(NonexistentJpaException.class)
                    .hasMessage("The indicated JPA does not exist.");
        }
    }

    @Nested
    @DisplayName("getJpaList / setJpaList")
    class GetSetList {

        @Test
        @DisplayName("should allow direct list manipulation")
        void shouldAllowDirectListAccess() {
            java.util.ArrayList<JpaPersistence> customList = new java.util.ArrayList<>();
            JpaAdministrator.setJpaList(customList);
            assertThat(JpaAdministrator.getJpaList()).isSameAs(customList);
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
