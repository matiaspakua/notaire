package com.licensis.notaire.unit;

import java.io.InputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApplicationResource (issue #1069)")
class ApplicationResourceTest {

    @Test
    @DisplayName("Should not include config.properties resource (legacy Swing-era file)")
    void shouldNotIncludeConfigProperties() {
        // Arrange: No setup needed - we're checking the classpath resource

        // Act: Look up the legacy config.properties resource
        String resourceName = "/config.properties";
        InputStream resource = getClass().getResourceAsStream(resourceName);

        // Assert: Test should FAIL because the legacy file currently exists
        assertThat(resource).isNull();
    }
}
