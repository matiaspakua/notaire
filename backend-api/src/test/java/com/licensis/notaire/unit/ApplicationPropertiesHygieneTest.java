package com.licensis.notaire.unit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationPropertiesHygiene (issue #1069)")
class ApplicationPropertiesHygieneTest {

    @Test
    @DisplayName("Should not define dead security user keys (spring.security.user.*) in application.properties")
    void shouldNotDefineDeadSecurityUserKeys() throws IOException {
        // Arrange
        Properties props = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            props.load(in);
        }

        // Act / Assert
        assertThat(props.stringPropertyNames()).noneMatch(key -> key.startsWith("spring.security.user."));
    }
}
