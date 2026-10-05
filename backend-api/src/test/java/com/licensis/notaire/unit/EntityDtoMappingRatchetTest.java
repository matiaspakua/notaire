package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

@DisplayName("Entities may only lose their DTO mapping methods, never gain them")
class EntityDtoMappingRatchetTest {

    private static final String ENTITIES_PACKAGE = "com.licensis.notaire.business";
    private static final String BASELINE = "/architecture/entity-dto-mapping.txt";
    private static final Set<String> MAPPING_NAMES = Set.of("getDto", "setAtributo", "setAtributos", "toDto");

    @Test
    @DisplayName("shouldNotAddEntityMappingMethods")
    void shouldNotAddEntityMappingMethods() throws IOException {
        assertThat(declaredMappingMethods()).as("mapping methods missing from the baseline").isSubsetOf(baseline());
    }

    @Test
    @DisplayName("shouldRemoveDeletedMethodsFromTheBaseline")
    void shouldRemoveDeletedMethodsFromTheBaseline() throws IOException {
        assertThat(baseline()).as("baseline entries no entity declares any more").isSubsetOf(declaredMappingMethods());
    }

    private static Set<String> declaredMappingMethods() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));
        return scanner.findCandidateComponents(ENTITIES_PACKAGE).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(name -> ClassUtils.resolveClassName(name, EntityDtoMappingRatchetTest.class.getClassLoader()))
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()).filter(EntityDtoMappingRatchetTest::isMapping)
                        .map(method -> type.getSimpleName() + "#" + method.getName()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static boolean isMapping(Method method) {
        return MAPPING_NAMES.contains(method.getName()) && !method.isSynthetic();
    }

    private static Set<String> baseline() throws IOException {
        try (InputStream in = EntityDtoMappingRatchetTest.class.getResourceAsStream(BASELINE)) {
            assertThat(in).as("baseline resource " + BASELINE).isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().map(String::strip)
                    .filter(line -> !line.isEmpty()).collect(Collectors.toCollection(TreeSet::new));
        }
    }
}
