package com.licensis.notaire.unit.folio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.licensis.notaire.adapter.out.persistence.folio.FolioTypePersistenceAdapter;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.domain.folio.FolioTypeInUseException;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;

@DisplayName("Folio type persistence adapter (issue #1703)")
class FolioTypePersistenceAdapterTest {

    private final FolioTypeRepository types = mock(FolioTypeRepository.class);
    private final FolioRepository folios = mock(FolioRepository.class);
    private final FolioTypePersistenceAdapter adapter = new FolioTypePersistenceAdapter(types, folios);

    private FolioType legacy(int id, String name, int version) {
        FolioType entity = new FolioType();
        entity.setIdFolioType(id);
        entity.setName(name);
        entity.setNotes("n");
        entity.setEnabled(true);
        entity.setIsAuxiliary(true);
        entity.setVersion(version);
        return entity;
    }

    @Test
    @DisplayName("maps every field of the legacy entity to the domain type")
    void mapsToDomain() {
        when(types.findById(3)).thenReturn(Optional.of(legacy(3, "Protocol", 2)));

        var type = adapter.findById(3).orElseThrow();

        assertThat(type.id()).isEqualTo(3);
        assertThat(type.name()).isEqualTo("Protocol");
        assertThat(type.notes()).isEqualTo("n");
        assertThat(type.enabled()).isTrue();
        assertThat(type.auxiliary()).isTrue();
        assertThat(type.version()).isEqualTo(2);
    }

    @Test
    @DisplayName("saving an existing type updates the loaded entity so JPA manages the version")
    void updatesLoadedEntity() {
        FolioType loaded = legacy(3, "Old", 2);
        when(types.findById(3)).thenReturn(Optional.of(loaded));
        when(types.save(any(FolioType.class))).thenAnswer(call -> call.getArgument(0));

        var saved = adapter.save(com.licensis.notaire.domain.folio.FolioType.restore(3, "New", null, false, false, 2));

        assertThat(loaded.getName()).isEqualTo("New");
        assertThat(loaded.getEnabled()).isFalse();
        assertThat(saved.name()).isEqualTo("New");
    }

    @Test
    @DisplayName("usage comes from the folios repository")
    void usage() {
        when(folios.findByFkIdFolioTypeIdFolioType(4)).thenReturn(List.of(new Folio()));
        when(folios.findByFkIdFolioTypeIdFolioType(5)).thenReturn(List.of());

        assertThat(adapter.isUsedByFolios(4)).isTrue();
        assertThat(adapter.isUsedByFolios(5)).isFalse();
    }

    @Test
    @DisplayName("a foreign key violation while deleting becomes the in-use domain exception")
    void integrityFailureIsInUse() {
        when(types.existsById(6)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("fk")).when(types).flush();

        assertThatThrownBy(() -> adapter.deleteById(6)).isInstanceOf(FolioTypeInUseException.class);
    }

    @Test
    @DisplayName("delete reports whether the type existed")
    void deleteReportsExistence() {
        when(types.existsById(7)).thenReturn(false);

        assertThat(adapter.deleteById(7)).isFalse();
    }
}
