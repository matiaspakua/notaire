package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@DisplayName("REST controllers keep JPA entities out of their API signatures")
class ControllerSignatureArchitectureTest {

    private static final String CONTROLLERS_PACKAGE = "com.licensis.notaire.adapter.in.web";
    private static final String ENTITIES_PACKAGE = "com.licensis.notaire.business";
    private static final String LOOSE_BASELINE = "/architecture/controller-loose-returns.txt";

    @Test
    @DisplayName("shouldNotExposeEntitiesInHandlerSignatures")
    void shouldNotExposeEntitiesInHandlerSignatures() {
        Set<String> leaks = handlerMethods()
                .filter(method -> signatureTypes(method).anyMatch(ControllerSignatureArchitectureTest::isEntity))
                .map(ControllerSignatureArchitectureTest::describe)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(leaks).as("handler methods whose signature mentions a business entity").isEmpty();
    }

    @Test
    @DisplayName("shouldNotAddLooselyTypedHandlerReturns")
    void shouldNotAddLooselyTypedHandlerReturns() throws IOException {
        Set<String> loose = handlerMethods()
                .filter(method -> returnTypes(method).anyMatch(ControllerSignatureArchitectureTest::isLoose))
                .map(ControllerSignatureArchitectureTest::describe)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(loose).as("loosely typed handler returns missing from the baseline").isSubsetOf(baseline());
    }

    @Test
    @DisplayName("shouldRemoveFixedMethodsFromTheLooseReturnBaseline")
    void shouldRemoveFixedMethodsFromTheLooseReturnBaseline() throws IOException {
        Set<String> loose = handlerMethods()
                .filter(method -> returnTypes(method).anyMatch(ControllerSignatureArchitectureTest::isLoose))
                .map(ControllerSignatureArchitectureTest::describe)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(baseline()).as("baseline entries that are no longer loose").isSubsetOf(loose);
    }

    private static Stream<Method> handlerMethods() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return scanner.findCandidateComponents(CONTROLLERS_PACKAGE).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(name -> ClassUtils.resolveClassName(name, ControllerSignatureArchitectureTest.class.getClassLoader()))
                .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class));
    }

    private static Stream<Type> signatureTypes(Method method) {
        return Stream.concat(Stream.of(method.getGenericReturnType()), Arrays.stream(method.getGenericParameterTypes()))
                .flatMap(ControllerSignatureArchitectureTest::flatten);
    }

    private static Stream<Type> returnTypes(Method method) {
        return flatten(method.getGenericReturnType());
    }

    private static Stream<Type> flatten(Type type) {
        if (type instanceof ParameterizedType parameterized) {
            return Stream.concat(Stream.of(parameterized.getRawType()),
                    Arrays.stream(parameterized.getActualTypeArguments()).flatMap(ControllerSignatureArchitectureTest::flatten));
        }
        if (type instanceof GenericArrayType array) {
            return flatten(array.getGenericComponentType());
        }
        if (type instanceof WildcardType wildcard) {
            return Stream.concat(Stream.of(wildcard), Arrays.stream(wildcard.getUpperBounds())
                    .flatMap(ControllerSignatureArchitectureTest::flatten));
        }
        return Stream.of(type);
    }

    private static boolean isEntity(Type type) {
        return type instanceof Class<?> clazz && clazz.getName().startsWith(ENTITIES_PACKAGE + ".");
    }

    private static boolean isLoose(Type type) {
        return type == Object.class
                || type instanceof WildcardType wildcard && wildcard.getUpperBounds()[0] == Object.class;
    }

    private static String describe(Method method) {
        return method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    private static Set<String> baseline() throws IOException {
        try (InputStream in = ControllerSignatureArchitectureTest.class.getResourceAsStream(LOOSE_BASELINE)) {
            assertThat(in).as("baseline resource " + LOOSE_BASELINE).isNotNull();
            List<String> lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().toList();
            return lines.stream().map(String::strip).filter(line -> !line.isEmpty())
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }
}
