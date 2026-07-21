package com.licensis.notaire.unit;

import com.licensis.notaire.config.ProductionCredentialsGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductionCredentialsGuard (issue #565)")
class ProductionCredentialsGuardTest {

    private ProductionCredentialsGuard guardWith(String environment, String datasourceUsername,
            String datasourcePassword, String actuatorUsername, String actuatorPassword) {
        ProductionCredentialsGuard guard = new ProductionCredentialsGuard();
        ReflectionTestUtils.setField(guard, "environment", environment);
        ReflectionTestUtils.setField(guard, "datasourceUsername", datasourceUsername);
        ReflectionTestUtils.setField(guard, "datasourcePassword", datasourcePassword);
        ReflectionTestUtils.setField(guard, "actuatorUsername", actuatorUsername);
        ReflectionTestUtils.setField(guard, "actuatorPassword", actuatorPassword);
        return guard;
    }

    @Test
    @DisplayName("Should reject default datasource password in production")
    void shouldRejectDefaultDatasourcePasswordInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", "notaire_app", "admin",
                "custom-actuator", "s3cur3-pass");

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password");
    }

    @Test
    @DisplayName("Should reject default actuator credentials in production")
    void shouldRejectDefaultActuatorCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", "notaire_app", "s3cur3-pass",
                "admin", "admin");

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("actuator.security.username")
                .hasMessageContaining("actuator.security.password");
    }

    @Test
    @DisplayName("Should accept non-default credentials in production")
    void shouldAcceptNonDefaultCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", "notaire_app", "s3cur3-pass",
                "custom-actuator", "s3cur3-pass2");

        assertThatCode(guard::validateCredentials).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not validate credentials outside production")
    void shouldSkipValidationOutsideProduction() {
        ProductionCredentialsGuard guard = guardWith("development", "admin", "admin", "admin", "admin");

        assertThatCode(guard::validateCredentials).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should treat environment check as case-insensitive")
    void shouldTreatEnvironmentAsCaseInsensitive() {
        ProductionCredentialsGuard guard = guardWith("PRODUCTION", "notaire_app", "admin",
                "custom-actuator", "s3cur3-pass");

        assertThatThrownBy(guard::validateCredentials).isInstanceOf(IllegalStateException.class);
    }
}
