package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.IdentificationTypeLookup;
import com.licensis.notaire.testing.RequirementCoverage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RequirementCoverage({"CU76"})
@DisplayName("IdentificationTypeLookup")
class IdentificationTypeLookupTest {

    private final IdentificationTypeLookup lookup = new IdentificationTypeLookup(() -> List.of(
            new IdentificationType(1, "DNI"),
            new IdentificationType(11, "DNI Extranjero"),
            new IdentificationType(2, "Pasaporte")));

    @Test
    @DisplayName("nameOf returns the name of the type whose id matches exactly")
    void shouldReturnNameOfExactIdWhenIdPrefixesAnotherId() {
        assertThat(lookup.nameOf(1)).isEqualTo("DNI");
        assertThat(lookup.nameOf(11)).isEqualTo("DNI Extranjero");
    }

    @Test
    @DisplayName("nameOf returns null for an unknown or null id")
    void shouldReturnNullNameWhenIdUnknownOrNull() {
        assertThat(lookup.nameOf(99)).isNull();
        assertThat(lookup.nameOf(null)).isNull();
    }

    @Test
    @DisplayName("idOf matches the name exactly, ignoring case")
    void shouldReturnIdOfExactNameWhenNameIsPrefixOfAnother() {
        assertThat(lookup.idOf("dni")).isEqualTo(1);
        assertThat(lookup.idOf("DNI Extranjero")).isEqualTo(11);
    }

    @Test
    @DisplayName("idOf returns 0 for an unknown or null name")
    void shouldReturnZeroIdWhenNameUnknownOrNull() {
        assertThat(lookup.idOf("unknown")).isZero();
        assertThat(lookup.idOf(null)).isZero();
    }

    @Test
    @DisplayName("an empty catalog yields no match")
    void shouldReturnNoMatchWhenCatalogIsEmpty() {
        IdentificationTypeLookup empty = new IdentificationTypeLookup(List::of);

        assertThat(empty.nameOf(1)).isNull();
        assertThat(empty.idOf("DNI")).isZero();
    }
}
