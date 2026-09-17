package com.licensis.notaire.unit.adapter.out.concept;

import com.licensis.notaire.adapter.out.persistence.concept.ConceptPersistenceAdapter;
import com.licensis.notaire.application.port.out.concept.ConceptRepositoryPort;
import com.licensis.notaire.business.Concept;
import com.licensis.notaire.repository.ConceptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Concept Persistence Adapter")
class ConceptPersistenceAdapterTest {

    @Mock
    private ConceptRepository repository;

    private ConceptRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new ConceptPersistenceAdapter(repository);
    }

    @Test
    @DisplayName("Should find all concepts")
    void shouldFindAll() {
        Concept concept1 = new Concept();
        concept1.setIdConcept(1);
        Concept concept2 = new Concept();
        concept2.setIdConcept(2);

        when(repository.findAll()).thenReturn(List.of(concept1, concept2));

        List<Concept> result = adapter.findAll();

        assertThat(result).hasSize(2).containsExactly(concept1, concept2);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Should find concept by ID")
    void shouldFindById() {
        Concept concept = new Concept();
        concept.setIdConcept(1);

        when(repository.findById(1)).thenReturn(Optional.of(concept));

        Optional<Concept> result = adapter.findById(1);

        assertThat(result).isPresent().contains(concept);
        verify(repository).findById(1);
    }

    @Test
    @DisplayName("Should find concepts by name")
    void shouldFindByNameContaining() {
        Concept concept = new Concept();
        concept.setIdConcept(1);
        concept.setName("Honorarios");

        when(repository.findByNameContaining("Honorarios")).thenReturn(List.of(concept));

        List<Concept> result = adapter.findByNameContaining("Honorarios");

        assertThat(result).hasSize(1).contains(concept);
        verify(repository).findByNameContaining("Honorarios");
    }

    @Test
    @DisplayName("Should check if concept exists")
    void shouldCheckExists() {
        when(repository.existsById(1)).thenReturn(true);

        boolean exists = adapter.existsById(1);

        assertThat(exists).isTrue();
        verify(repository).existsById(1);
    }

    @Test
    @DisplayName("Should create concept")
    void shouldCreate() {
        Concept concept = new Concept();
        concept.setName("Notaría");
        Concept savedConcept = new Concept();
        savedConcept.setIdConcept(1);
        savedConcept.setName("Notaría");

        when(repository.save(concept)).thenReturn(savedConcept);

        Concept result = adapter.create(concept);

        assertThat(result).isNotNull().isEqualTo(savedConcept);
        verify(repository).save(concept);
    }

    @Test
    @DisplayName("Should update concept")
    void shouldUpdate() {
        Concept concept = new Concept();
        concept.setName("Updated");
        Concept updated = new Concept();
        updated.setIdConcept(1);
        updated.setName("Updated");

        when(repository.save(concept)).thenReturn(updated);

        Concept result = adapter.update(1, concept);

        assertThat(result.getIdConcept()).isEqualTo(1);
        verify(repository).save(concept);
    }

    @Test
    @DisplayName("Should delete concept by ID")
    void shouldDeleteById() {
        adapter.deleteById(1);

        verify(repository).deleteById(1);
    }
}
