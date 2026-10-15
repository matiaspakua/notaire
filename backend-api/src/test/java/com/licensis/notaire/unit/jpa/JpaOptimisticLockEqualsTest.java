package com.licensis.notaire.unit.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.jpa.OptimisticLockVersions;

/**
 * Pins hex-safe optimistic-lock compares for legacy JpaControllers (#1758 / java:S4973).
 */
@DisplayName("JpaControllers — boxed version equality (#1758)")
class JpaOptimisticLockEqualsTest {

    @Test
    @DisplayName("OptimisticLockVersions.differ uses value equality for boxed Integers")
    void differUsesValueEquality() {
        assertThat(OptimisticLockVersions.differ(1, 1)).isFalse();
        assertThat(OptimisticLockVersions.differ(Integer.valueOf(128), Integer.valueOf(128))).isFalse();
        assertThat(OptimisticLockVersions.differ(1, 2)).isTrue();
        assertThat(OptimisticLockVersions.differ(null, 1)).isTrue();
        assertThat(OptimisticLockVersions.differ(null, null)).isFalse();
    }

    @Test
    @DisplayName("no JpaController compares version with != (S4973 / RC_REF_COMPARISON)")
    void noReferenceInequalityOnVersionFields() throws IOException {
        Path jpaDir = Path.of("src/main/java/com/licensis/notaire/jpa");
        assertThat(jpaDir).isDirectory();
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(jpaDir)) {
            files.filter(p -> p.getFileName().toString().endsWith("JpaController.java")).forEach(path -> {
                try {
                    List<String> lines = Files.readAllLines(path);
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        if (line.contains("version != oldVersion") || line.contains("oldVersion != version")) {
                            offenders.add(path.getFileName() + ":" + (i + 1) + " — " + line.trim());
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(path.toString(), e);
                }
            });
        }
        assertThat(offenders).as("replace with OptimisticLockVersions.differ").isEmpty();
    }
}
