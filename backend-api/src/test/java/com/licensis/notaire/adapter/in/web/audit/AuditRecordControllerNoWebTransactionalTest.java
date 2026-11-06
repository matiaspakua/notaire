package com.licensis.notaire.adapter.in.web.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Issue #578 — transaction demarcation must not live in the HTTP adapter tier.
 */
@DisplayName("adapter.in.web has no @Transactional (issue #578)")
class AuditRecordControllerNoWebTransactionalTest {

    @Test
    @DisplayName("no @Transactional under adapter/in/web sources (#578)")
    void noTransactionalOnWebAdapters() throws IOException {
        Path webRoot = Path.of("src/main/java/com/licensis/notaire/adapter/in/web");
        assertThat(webRoot).exists();

        List<String> offenders = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(webRoot)) {
            paths.filter(p -> p.toString().endsWith(".java")).forEach(path -> {
                try {
                    String text = Files.readString(path);
                    if (text.contains("@Transactional")) {
                        offenders.add(webRoot.relativize(path).toString());
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(path.toString(), e);
                }
            });
        }

        assertThat(offenders)
                .as("@Transactional must live in application use cases, not web adapters (#578)")
                .isEmpty();
    }
}
