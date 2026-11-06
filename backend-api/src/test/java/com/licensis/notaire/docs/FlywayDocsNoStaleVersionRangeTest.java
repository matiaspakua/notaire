package com.licensis.notaire.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards Flyway docs against the stale {@code V1→V11+} range (issue #1980).
 */
@DisplayName("Flyway docs omit stale V1→V11+ range (#1980)")
class FlywayDocsNoStaleVersionRangeTest {

    private static final String STALE = "V1→V11+";

    private static Path repoRoot() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (Files.isRegularFile(cwd.resolve("src/main/resources/application.properties"))) {
            return cwd.getParent(); // backend-api → repo root
        }
        return cwd;
    }

    @Test
    @DisplayName("classpath application.properties omits V1→V11+")
    void applicationPropertiesOmitsStaleRange() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            assertThat(in).as("application.properties on test classpath").isNotNull();
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(text).doesNotContain(STALE);
        }
    }

    @Test
    @DisplayName("migration README and database-migrations rule omit V1→V11+")
    void readmeAndRuleOmitStaleRange() throws IOException {
        Path root = repoRoot();
        for (String rel : new String[] {
            "backend-api/src/main/resources/db/migration/README.md",
            ".claude/rules/database-migrations.md"
        }) {
            Path path = root.resolve(rel);
            String text = Files.readString(path, StandardCharsets.UTF_8);
            assertThat(text).as("%s", rel).doesNotContain(STALE);
        }
        String readme = Files.readString(
                root.resolve("backend-api/src/main/resources/db/migration/README.md"), StandardCharsets.UTF_8);
        assertThat(readme).contains("/docs/database/");
        assertThat(readme).contains("R{n}__");
    }
}
