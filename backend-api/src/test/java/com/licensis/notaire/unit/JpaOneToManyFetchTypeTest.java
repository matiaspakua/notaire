package com.licensis.notaire.unit;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.business.DocumentType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #595: no {@code @OneToMany} collection in the live entity model uses {@code EAGER}.
 */
@DisplayName("JPA OneToMany fetch types (#595)")
class JpaOneToManyFetchTypeTest {

    @Test
    @DisplayName("Concept.budgetTemplateList is LAZY")
    void conceptBudgetTemplatesAreLazy() throws Exception {
        assertLazyOneToMany(Concept.class, "budgetTemplateList");
    }

    @Test
    @DisplayName("DocumentType.procedureTemplateList is LAZY")
    void documentTypeProcedureTemplatesAreLazy() throws Exception {
        assertLazyOneToMany(DocumentType.class, "procedureTemplateList");
    }

    private static void assertLazyOneToMany(Class<?> entity, String fieldName) throws Exception {
        Field field = entity.getDeclaredField(fieldName);
        OneToMany mapping = field.getAnnotation(OneToMany.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.fetch()).isEqualTo(FetchType.LAZY);
    }
}
