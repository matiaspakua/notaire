package com.licensis.notaire.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Epic #1693 / issue #1695: the dependency rules of the backend hexagon (ADR-030). Every rule
 * compares the dependencies it finds with {@code architecture/hexagon-baseline.txt}: a new violation
 * fails the build, and so does a baseline entry that no longer exists, so the baseline can only shrink.
 *
 * <p>Rewrite the baseline with {@code -Darchitecture.baseline=shrink} (drops fixed entries, never adds)
 * or {@code -Darchitecture.baseline=init} (writes everything; only for the first generation).
 */
@DisplayName("Backend follows the hexagon; violations only shrink (epic #1693)")
class HexagonArchitectureTest {

    private static final String ROOT = "com.licensis.notaire";
    private static final String BASELINE_RESOURCE = "/architecture/hexagon-baseline.txt";
    private static final String BASELINE_FILE = "src/test/resources/architecture/hexagon-baseline.txt";
    private static final String WRITE_MODE = System.getProperty("architecture.baseline", "");

    private static final String ADAPTER_IN = ROOT + ".adapter.in";
    private static final String ADAPTER_OUT = ROOT + ".adapter.out";
    private static final String APPLICATION = ROOT + ".application";
    private static final String DOMAIN = ROOT + ".domain";
    private static final String LEGACY_JPA = ROOT + ".jpa";
    private static final String LEGACY_SERVICE = ROOT + ".service";
    private static final String SPRING_DATA_REPOSITORIES = ROOT + ".repository";
    private static final String JPA_ENTITIES = ROOT + ".business";

    private static final Set<String> ALLOWED_SPRING_IN_APPLICATION = Set.of(
            "org.springframework.stereotype.Service",
            "org.springframework.transaction.annotation.Transactional");

    private static JavaClasses production;
    private static Set<String> baseline;
    private static final Set<String> allViolations = new TreeSet<>();

    @BeforeAll
    static void importProduction() throws IOException {
        production = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
        baseline = readBaseline();
    }

    private static boolean in(JavaClass type, String... packages) {
        for (String candidate : packages) {
            if (type.getPackageName().equals(candidate) || type.getPackageName().startsWith(candidate + ".")) {
                return true;
            }
        }
        return false;
    }

    private static String topLevel(JavaClass type) {
        return type.getName().replaceAll("\\$.*", "");
    }

    private static Set<String> dependencies(Predicate<JavaClass> origin, Predicate<JavaClass> forbiddenTarget) {
        return production.stream()
                .filter(origin)
                .flatMap(type -> type.getDirectDependenciesFromSelf().stream())
                .filter(dependency -> forbiddenTarget.test(dependency.getTargetClass()))
                .filter(dependency -> !topLevel(dependency.getOriginClass()).equals(topLevel(dependency.getTargetClass())))
                .map(HexagonArchitectureTest::describe)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static String describe(Dependency dependency) {
        return topLevel(dependency.getOriginClass()) + " -> " + topLevel(dependency.getTargetClass());
    }

    private static Set<String> readBaseline() throws IOException {
        try (InputStream stream = HexagonArchitectureTest.class.getResourceAsStream(BASELINE_RESOURCE)) {
            if (stream == null) {
                return new TreeSet<>();
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private void assertAgainstBaseline(String rule, Set<String> found) {
        Set<String> tagged = found.stream().map(line -> rule + " | " + line)
                .collect(Collectors.toCollection(TreeSet::new));
        allViolations.addAll(tagged);
        if (!WRITE_MODE.isEmpty()) {
            return;
        }
        Set<String> known = baseline.stream().filter(line -> line.startsWith(rule + " | "))
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> added = new TreeSet<>(tagged);
        added.removeAll(known);
        Set<String> fixed = new TreeSet<>(known);
        fixed.removeAll(tagged);

        assertThat(added).as("%s: new violations (fix them, do not add to the baseline)", rule).isEmpty();
        assertThat(fixed).as("%s: baseline entries that no longer exist (remove them: "
                + "mvn test -pl backend-api -Dtest=HexagonArchitectureTest -Darchitecture.baseline=shrink)", rule)
                .isEmpty();
    }

    @Test
    @DisplayName("R1 inbound adapters do not reach repositories, legacy JPA or entities")
    void inboundAdaptersUseOnlyInboundPorts() {
        assertAgainstBaseline("R1", dependencies(type -> in(type, ADAPTER_IN),
                target -> in(target, SPRING_DATA_REPOSITORIES, LEGACY_JPA, JPA_ENTITIES, "jakarta.persistence")));
    }

    @Test
    @DisplayName("R2 the application layer depends on domain and ports only, plus Service and Transactional")
    void applicationDependsOnDomainAndPortsOnly() {
        assertAgainstBaseline("R2", dependencies(type -> in(type, APPLICATION),
                target -> in(target, SPRING_DATA_REPOSITORIES, LEGACY_JPA, LEGACY_SERVICE, JPA_ENTITIES, ADAPTER_IN,
                        ADAPTER_OUT, "jakarta.persistence", "org.hibernate", "com.fasterxml.jackson")
                        || (in(target, "org.springframework")
                                && !ALLOWED_SPRING_IN_APPLICATION.contains(topLevel(target)))));
    }

    @Test
    @DisplayName("R3 the domain depends on nothing but the JDK and itself")
    void domainIsPure() {
        assertAgainstBaseline("R3", dependencies(type -> in(type, DOMAIN),
                target -> !in(target, DOMAIN, "java") && !target.isPrimitive()));
    }

    @Test
    @DisplayName("R4 outbound adapters do not depend on inbound adapters")
    void outboundAdaptersDoNotKnowInboundOnes() {
        assertAgainstBaseline("R4", dependencies(type -> in(type, ADAPTER_OUT), target -> in(target, ADAPTER_IN)));
    }

    @Test
    @DisplayName("R5 inbound adapters do not depend on outbound adapters")
    void inboundAdaptersDoNotKnowOutboundOnes() {
        assertAgainstBaseline("R5", dependencies(type -> in(type, ADAPTER_IN), target -> in(target, ADAPTER_OUT)));
    }

    @Test
    @DisplayName("R6 nothing new depends on the legacy jpa and service packages")
    void legacyPackagesGainNoDependents() {
        assertAgainstBaseline("R6", dependencies(type -> in(type, ROOT) && !in(type, LEGACY_JPA, LEGACY_SERVICE),
                target -> in(target, LEGACY_JPA, LEGACY_SERVICE)));
    }

    @Test
    @DisplayName("the baseline file is sorted, has no duplicates and uses known rule ids")
    void baselineIsWellFormed() throws IOException {
        List<String> lines = Files.exists(Path.of(BASELINE_FILE))
                ? Files.readAllLines(Path.of(BASELINE_FILE)).stream().filter(line -> !line.isBlank()).toList()
                : List.of();

        assertThat(lines).isSorted().doesNotHaveDuplicates()
                .allMatch(line -> line.matches("R[1-6] \\| \\S+ -> \\S+"));
    }
}
