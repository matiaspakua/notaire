package com.licensis.notaire.unit.application.usecase.concept;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.application.port.out.concept.ConceptRepositoryPort;
import com.licensis.notaire.application.usecase.concept.ConceptService;
import com.licensis.notaire.business.Concept;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Concept Service")
class ConceptServiceTest {

    @Mock
    private ConceptRepositoryPort repository;

    private ConceptService service;

    @BeforeEach
    void setUp() {
        service = new ConceptService(repository);
    }

    @Test
    @DisplayName("Should find all concepts")
    void shouldFindAll() {
        Concept concept1 = new Concept();
        concept1.setIdConcept(1);
        Concept concept2 = new Concept();
        concept2.setIdConcept(2);

        when(repository.findAll()).thenReturn(List.of(concept1, concept2));

        List<Concept> result = service.findAll();

        assertThat(result).hasSize(2).containsExactly(concept1, concept2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should find concept by ID")
    void shouldFindById() {
        Concept concept = new Concept();
        concept.setIdConcept(1);

        when(repository.findById(1)).thenReturn(Optional.of(concept));

        Optional<Concept> result = service.findById(1);

        assertThat(result).isPresent().contains(concept);
        verify(repository).findById(1);
    }

    @Test
    @DisplayName("Should search concepts by name")
    void shouldSearchByName() {
        Concept concept = new Concept();
        concept.setIdConcept(1);
        concept.setName("Honorarios");

        when(repository.findByNameContaining("Honorarios")).thenReturn(List.of(concept));

        List<Concept> result = service.searchByName("Honorarios");

        assertThat(result).hasSize(1).contains(concept);
        verify(repository).findByNameContaining("Honorarios");
    }

    @Test
    @DisplayName("Should create concept with enabled flag")
    void shouldCreateConcept() {
        Concept concept = new Concept();
        concept.setName("Notaría");
        Concept savedConcept = new Concept();
        savedConcept.setIdConcept(1);
        savedConcept.setName("Notaría");
        savedConcept.setEnabled(true);

        when(repository.create(concept)).thenReturn(savedConcept);

        Concept result = service.create(concept);

        assertThat(result).isNotNull();
        assertThat(result.getIdConcept()).isEqualTo(1);
        assertThat(result.getEnabled()).isTrue();
        verify(repository).create(concept);
    }

    @Test
    @DisplayName("Should update existing concept")
    void shouldUpdateConcept() {
        Concept concept = new Concept();
        concept.setName("Updated");
        Concept updated = new Concept();
        updated.setIdConcept(1);
        updated.setName("Updated");

        when(repository.existsById(1)).thenReturn(true);
        when(repository.update(1, concept)).thenReturn(updated);

        Concept result = service.update(1, concept);

        assertThat(result.getIdConcept()).isEqualTo(1);
        verify(repository).existsById(1);
        verify(repository).update(1, concept);
    }

    @Test
    @DisplayName("Should throw when updating non-existent concept")
    void shouldThrowWhenUpdatingNonExistent() {
        Concept concept = new Concept();
        when(repository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> service.update(999, concept))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Concept not found with ID: 999");

        verify(repository).existsById(999);
    }

    @Test
    @DisplayName("Should delete existing concept")
    void shouldDeleteConcept() {
        when(repository.existsById(1)).thenReturn(true);

        service.deleteById(1);

        verify(repository).existsById(1);
        verify(repository).deleteById(1);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent concept")
    void shouldThrowWhenDeletingNonExistent() {
        when(repository.existsById(999)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Concept not found with ID: 999");

        verify(repository).existsById(999);
    }
}
