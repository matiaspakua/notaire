package com.licensis.notaire.unit.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.property.PropertyCatalogAdapter;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.domain.property.PropertyDetails;
import com.licensis.notaire.repository.PropertyRepository;

@DisplayName("Property catalog adapter (issue #1702)")
class PropertyPersistenceAdapterTest {

    private final PropertyRepository repository = mock(PropertyRepository.class);
    private final PropertyCatalogAdapter adapter = new PropertyCatalogAdapter(repository);

    private Property legacy(int id, String designation, int version) {
        Property entity = new Property(id);
        entity.setCadastralDesignation(designation);
        entity.setFiscalAppraisal(new BigDecimal("5"));
        entity.setAddress("St");
        entity.setVersion(version);
        return entity;
    }

    @Test
    @DisplayName("maps every field of the legacy entity to the domain type")
    void mapsToDomain() {
        when(repository.findById(3)).thenReturn(Optional.of(legacy(3, "CAD", 2)));

        var property = adapter.findById(3).orElseThrow();

        assertThat(property.id()).isEqualTo(3);
        assertThat(property.version()).isEqualTo(2);
        assertThat(property.details().cadastralDesignation()).isEqualTo("CAD");
        assertThat(property.details().fiscalAppraisal()).isEqualByComparingTo("5");
        assertThat(property.details().address()).isEqualTo("St");
    }

    @Test
    @DisplayName("saving an existing property updates the loaded entity so JPA manages the version")
    void updatesLoadedEntity() {
        Property loaded = legacy(3, "OLD", 2);
        when(repository.findById(3)).thenReturn(Optional.of(loaded));
        when(repository.save(any(Property.class))).thenAnswer(call -> call.getArgument(0));

        var saved = adapter.save(com.licensis.notaire.domain.property.Property.restore(3,
                new PropertyDetails("NEW", null, "Other", null, null, null, null), 2));

        assertThat(loaded.getCadastralDesignation()).isEqualTo("NEW");
        assertThat(loaded.getFiscalAppraisal()).isNull();
        assertThat(saved.details().cadastralDesignation()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("delete reports whether the property existed")
    void deleteReportsExistence() {
        when(repository.existsById(4)).thenReturn(true);
        when(repository.existsById(5)).thenReturn(false);

        assertThat(adapter.deleteById(4)).isTrue();
        assertThat(adapter.deleteById(5)).isFalse();
        verify(repository).deleteById(4);
    }
}
