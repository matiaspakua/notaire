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
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * R8 (#2484): nothing outside the legacy packages gains a dependency on {@code repository}, {@code business} or
 * {@code dto}. That covers ports and use cases that speak legacy web DTOs (ADR-030) and new users of the JPA model
 * outside the persistence adapters.
 *
 * <p>The dependencies that exist today are listed in {@code legacy-dependents-baseline.txt}. A new one fails, and
 * so does a listed one that is gone. The file is its own {@code aisdlc} ratchet, so it can only shrink
 * ({@code -Darchitecture.baseline=shrink}); {@code init} was used once, for the first generation.
 */
@DisplayName("Legacy packages gain no new dependents; the baseline only shrinks (#2484)")
class LegacyPackageArchitectureTest {

    private static final String ROOT = "com.licensis.notaire";
    private static final Set<String> LEGACY = Set.of(ROOT + ".jpa", ROOT + ".service", ROOT + ".repository",
            ROOT + ".business", ROOT + ".dto");
    private static final String BASELINE_RESOURCE = "/architecture/legacy-dependents-baseline.txt";
    private static final String BASELINE_FILE = "src/test/resources/architecture/legacy-dependents-baseline.txt";
    private static final String WRITE_MODE = System.getProperty("architecture.baseline", "");

    private static JavaClasses production;

    @BeforeAll
    static void importProduction() {
        production = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(ROOT);
    }

    private static boolean isLegacy(JavaClass type) {
        String name = type.getPackageName();
        return LEGACY.stream().anyMatch(p -> name.equals(p) || name.startsWith(p + "."));
    }

    private static String topLevel(JavaClass type) {
        return type.getName().replaceAll("\\$.*", "");
    }

    static Set<String> violations(JavaClasses classes) {
        return classes.stream()
                .filter(type -> type.getPackageName().startsWith(ROOT) && !isLegacy(type))
                .flatMap(type -> type.getDirectDependenciesFromSelf().stream())
                .filter(dependency -> isLegacy(dependency.getTargetClass()))
                .map(dependency -> "R8 | " + topLevel(dependency.getOriginClass()) + " -> "
                        + topLevel(dependency.getTargetClass()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Set<String> readBaseline() throws IOException {
        try (InputStream stream = LegacyPackageArchitectureTest.class.getResourceAsStream(BASELINE_RESOURCE)) {
            if (stream == null) {
                return new TreeSet<>();
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).lines().map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    @Test
    @DisplayName("R8 nothing new depends on the legacy repository, business and dto packages")
    void legacyPackagesGainNoNewDependents() throws IOException {
        Set<String> found = violations(production);
        Set<String> baseline = readBaseline();
        if ("init".equals(WRITE_MODE) || "shrink".equals(WRITE_MODE)) {
            Set<String> next = new TreeSet<>(found);
            if ("shrink".equals(WRITE_MODE)) {
                next.retainAll(baseline);
            }
            Files.write(Path.of(BASELINE_FILE), (String.join("\n", next) + (next.isEmpty() ? "" : "\n"))
                    .getBytes(StandardCharsets.UTF_8));
            return;
        }
        Set<String> added = new TreeSet<>(found);
        added.removeAll(baseline);
        Set<String> fixed = new TreeSet<>(baseline);
        fixed.removeAll(found);

        assertThat(added).as("R8: new dependents of a legacy package (fix them, do not add to the baseline)")
                .isEmpty();
        assertThat(fixed).as("R8: baseline entries that no longer exist (remove them: mvn test -pl backend-api "
                + "-Dtest=LegacyPackageArchitectureTest -Darchitecture.baseline=shrink)").isEmpty();
    }

    @Test
    @DisplayName("the baseline is sorted, has no duplicates and names R8")
    void baselineIsWellFormed() throws IOException {
        List<String> lines = Files.exists(Path.of(BASELINE_FILE))
                ? Files.readAllLines(Path.of(BASELINE_FILE)).stream().filter(line -> !line.isBlank()).toList()
                : List.of();
        assertThat(lines).isSorted().doesNotHaveDuplicates().allMatch(line -> line.matches("R8 \\| \\S+ -> \\S+"));
    }
}
