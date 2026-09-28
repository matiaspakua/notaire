package com.licensis.notaire.unit;

import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationResource (issue #1069)")
class ApplicationResourceTest {

    @Test
    @DisplayName("Should not include config.properties resource (legacy Swing-era file)")
    void shouldNotIncludeConfigProperties() {
        // Arrange: No setup needed - we're checking the classpath resource

        // Act
        URL resource = getClass().getResource("/config.properties");

        // Assert
        assertThat(resource).isNull();
    }
}
