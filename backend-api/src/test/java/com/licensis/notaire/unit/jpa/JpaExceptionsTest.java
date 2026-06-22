package com.licensis.notaire.unit.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JPA exception classes unit tests")
class JpaExceptionsTest {

    @Nested
    @DisplayName("IllegalOrphanException")
    class IllegalOrphanExceptionTests {

        @Test
        @DisplayName("Should store messages list and return first as message")
        void shouldStoreMessagesAndReturnFirstAsMessage() {
            List<String> messages = List.of("Cannot remove orphan A", "Cannot remove orphan B");
            IllegalOrphanException ex = new IllegalOrphanException(messages);

            assertThat(ex.getMessage()).isEqualTo("Cannot remove orphan A");
            assertThat(ex.getMessages()).containsExactly("Cannot remove orphan A", "Cannot remove orphan B");
        }

        @Test
        @DisplayName("Should handle null messages by creating empty list")
        void shouldHandleNullMessagesByCreatingEmptyList() {
            IllegalOrphanException ex = new IllegalOrphanException(null);

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getMessages()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty messages list with null message")
        void shouldHandleEmptyMessagesList() {
            IllegalOrphanException ex = new IllegalOrphanException(List.of());

            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getMessages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("CreateEntityException")
    class CreateEntityExceptionTests {

        @Test
        @DisplayName("Default constructor should create exception without message or cause")
        void defaultConstructor() {
            CreateEntityException ex = new CreateEntityException();
            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should accept message only")
        void shouldAcceptMessageOnly() {
            CreateEntityException ex = new CreateEntityException("Creation failed");
            assertThat(ex.getMessage()).isEqualTo("Creation failed");
        }

        @Test
        @DisplayName("Should accept message and cause")
        void shouldAcceptMessageAndCause() {
            Throwable cause = new RuntimeException("DB error");
            CreateEntityException ex = new CreateEntityException("Creation failed", cause);
            assertThat(ex.getMessage()).isEqualTo("Creation failed");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Should accept cause only")
        void shouldAcceptCauseOnly() {
            Throwable cause = new RuntimeException("DB error");
            CreateEntityException ex = new CreateEntityException(cause);
            assertThat(ex.getMessage()).isEqualTo("java.lang.RuntimeException: DB error");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Should accept full constructor with suppression and writable stack trace")
        void shouldAcceptFullConstructor() {
            Throwable cause = new RuntimeException("DB error");
            CreateEntityException ex = new CreateEntityException("Creation failed", cause, true, true);
            assertThat(ex.getMessage()).isEqualTo("Creation failed");
            assertThat(ex.getCause()).isSameAs(cause);
            assertThat(ex.getSuppressed()).isEmpty();
        }
    }

    @Nested
    @DisplayName("NonexistentEntityException")
    class NonexistentEntityExceptionTests {

        @Test
        @DisplayName("Should accept message only")
        void shouldAcceptMessageOnly() {
            NonexistentEntityException ex = new NonexistentEntityException("Entity not found");
            assertThat(ex.getMessage()).isEqualTo("Entity not found");
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should accept message and cause")
        void shouldAcceptMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            NonexistentEntityException ex = new NonexistentEntityException("Entity not found", cause);
            assertThat(ex.getMessage()).isEqualTo("Entity not found");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("NonexistentJpaException")
    class NonexistentJpaExceptionTests {

        @Test
        @DisplayName("Default constructor should create exception without message or cause")
        void defaultConstructor() {
            NonexistentJpaException ex = new NonexistentJpaException();
            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should accept message only")
        void shouldAcceptMessageOnly() {
            NonexistentJpaException ex = new NonexistentJpaException("JPA not found");
            assertThat(ex.getMessage()).isEqualTo("JPA not found");
        }

        @Test
        @DisplayName("Should accept message and cause")
        void shouldAcceptMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            NonexistentJpaException ex = new NonexistentJpaException("JPA not found", cause);
            assertThat(ex.getMessage()).isEqualTo("JPA not found");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test
        @DisplayName("Should accept cause only")
        void shouldAcceptCauseOnly() {
            Throwable cause = new RuntimeException("Root cause");
            NonexistentJpaException ex = new NonexistentJpaException(cause);
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ClassEliminatedException")
    class ClassEliminatedExceptionTests {

        @Test
        @DisplayName("Default constructor should create exception without message or cause")
        void defaultConstructor() {
            ClassEliminatedException ex = new ClassEliminatedException();
            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should accept message only")
        void shouldAcceptMessageOnly() {
            ClassEliminatedException ex = new ClassEliminatedException("Entity was eliminated");
            assertThat(ex.getMessage()).isEqualTo("Entity was eliminated");
        }

        @Test
        @DisplayName("Should accept message and cause")
        void shouldAcceptMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            ClassEliminatedException ex = new ClassEliminatedException("Entity was eliminated", cause);
            assertThat(ex.getMessage()).isEqualTo("Entity was eliminated");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("ClassModifiedException")
    class ClassModifiedExceptionTests {

        @Test
        @DisplayName("Default constructor should create exception without message or cause")
        void defaultConstructor() {
            ClassModifiedException ex = new ClassModifiedException();
            assertThat(ex.getMessage()).isNull();
            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("Should accept message only")
        void shouldAcceptMessageOnly() {
            ClassModifiedException ex = new ClassModifiedException("Entity was modified");
            assertThat(ex.getMessage()).isEqualTo("Entity was modified");
        }

        @Test
        @DisplayName("Should accept message and cause")
        void shouldAcceptMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            ClassModifiedException ex = new ClassModifiedException("Entity was modified", cause);
            assertThat(ex.getMessage()).isEqualTo("Entity was modified");
            assertThat(ex.getCause()).isSameAs(cause);
        }
    }
}
