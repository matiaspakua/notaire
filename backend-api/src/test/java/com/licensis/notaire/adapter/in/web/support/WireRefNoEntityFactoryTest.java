package com.licensis.notaire.adapter.in.web.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Support *Ref records must not factory-map from JPA entities (issue #1826 / ArchUnit R1).
 */
@DisplayName("Wire *Ref factories (issue #1826)")
class WireRefNoEntityFactoryTest {

    private static final List<Class<?>> REFS = List.of(
            DeedRef.class,
            DocumentTypeRef.class,
            ManagementRef.class,
            NotaryRef.class,
            ProcedureTypeRef.class,
            PropertyRef.class);

    @Test
    @DisplayName("Should not declare from() overloads that take business.* entities")
    void shouldNotDeclareEntityFromFactories() {
        for (Class<?> ref : REFS) {
            Method[] methods = ref.getDeclaredMethods();
            assertThat(Arrays.stream(methods)
                            .filter(m -> m.getName().equals("from"))
                            .map(m -> m.getParameterTypes()[0].getName())
                            .filter(name -> name.startsWith("com.licensis.notaire.business."))
                            .toList())
                    .as("%s must not have from(business.*)", ref.getSimpleName())
                    .isEmpty();
        }
    }
}
