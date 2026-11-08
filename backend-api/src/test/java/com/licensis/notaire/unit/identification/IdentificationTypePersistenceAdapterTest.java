package com.licensis.notaire.unit.identification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.licensis.notaire.adapter.out.persistence.identification.IdentificationTypePersistenceAdapter;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.domain.identification.IdentificationTypeInUseException;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.PersonRepository;

@DisplayName("Identification type persistence adapter (issue #1700)")
class IdentificationTypePersistenceAdapterTest {

    private final IdentificationTypeRepository types = mock(IdentificationTypeRepository.class);
    private final PersonRepository people = mock(PersonRepository.class);
    private final IdentificationTypePersistenceAdapter adapter = new IdentificationTypePersistenceAdapter(types, people);

    private IdentificationType legacy(int id, String name, String characters, int version) {
        IdentificationType entity = new IdentificationType();
        entity.setIdIdentificationType(id);
        entity.setName(name);
        entity.setCharacters(characters);
        entity.setVersion(version);
        return entity;
    }

    @Test
    @DisplayName("maps the legacy entity to the domain type")
    void mapsToDomain() {
        when(types.findById(3)).thenReturn(Optional.of(legacy(3, "DNI", "8", 2)));

        var type = adapter.findById(3).orElseThrow();

        assertThat(type.id()).isEqualTo(3);
        assertThat(type.name()).isEqualTo("DNI");
        assertThat(type.characters()).isEqualTo("8");
        assertThat(type.version()).isEqualTo(2);
    }

    @Test
    @DisplayName("a foreign key violation while deleting becomes the in-use domain exception")
    void integrityFailureIsInUse() {
        doThrow(new DataIntegrityViolationException("fk")).when(types).flush();

        assertThatThrownBy(() -> adapter.deleteById(5)).isInstanceOf(IdentificationTypeInUseException.class);
    }

    @Test
    @DisplayName("usage comes from the people repository")
    void usage() {
        when(people.existsByFkIdIdentificationTypeIdIdentificationType(4)).thenReturn(true);

        assertThat(adapter.isUsedByPeople(4)).isTrue();
        assertThat(adapter.isUsedByPeople(5)).isFalse();
    }

    @Test
    @DisplayName("saving an existing type updates the loaded entity so the version is managed by JPA")
    void updatesLoadedEntity() {
        IdentificationType loaded = legacy(3, "DNI", "8", 2);
        when(types.findById(3)).thenReturn(Optional.of(loaded));
        when(types.save(any(IdentificationType.class))).thenAnswer(call -> call.getArgument(0));

        var saved = adapter.save(com.licensis.notaire.domain.identification.IdentificationType
                .restore(3, "National id", "9", 2));

        assertThat(loaded.getName()).isEqualTo("National id");
        assertThat(saved.name()).isEqualTo("National id");
    }
}
