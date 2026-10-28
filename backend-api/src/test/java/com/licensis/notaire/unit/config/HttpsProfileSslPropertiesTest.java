package com.licensis.notaire.unit.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = HttpsProfileSslPropertiesTest.HttpsProfilePropertiesConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("https")
@DisplayName("HTTPS profile loads server.ssl properties (#254)")
class HttpsProfileSslPropertiesTest {

    @Profile("https")
    @Configuration
    @PropertySource("classpath:application-https.properties")
    static class HttpsProfilePropertiesConfig {}

    @Autowired
    private Environment environment;

    @Test
    void shouldLoadSslPropertiesWhenHttpsProfileIsActive() {
        assertThat(environment.getProperty("server.ssl.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("server.ssl.key-store-type")).isEqualTo("PKCS12");
        assertThat(environment.getProperty("server.ssl.key-store")).isNotBlank();
        assertThat(environment.getProperty("server.ssl.key-store-password")).isNotBlank();
        assertThat(environment.getProperty("server.ssl.key-alias")).isEqualTo("notaire-local");
        assertThat(environment.getProperty("server.port")).isEqualTo("8443");
    }
}
