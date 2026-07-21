package com.licensis.notaire.unit;

import com.licensis.notaire.config.ProductionCredentialsGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductionCredentialsGuard (issues #565, #651)")
class ProductionCredentialsGuardTest {

    private static final String SAFE_DATASOURCE_USERNAME = "notaire_app";
    private static final String SAFE_DATASOURCE_PASSWORD = "s3cur3-pass";
    private static final String SAFE_ACTUATOR_USERNAME = "custom-actuator";
    private static final String SAFE_ACTUATOR_PASSWORD = "s3cur3-pass2";
    private static final String SAFE_ADMIN_USERNAME = "custom-admin";
    private static final String SAFE_ADMIN_PASSWORD = "s3cur3-pass3";

    private ProductionCredentialsGuard guardWith(String environment) {
        return guardWith(environment, SAFE_DATASOURCE_USERNAME, SAFE_DATASOURCE_PASSWORD,
                SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME, SAFE_ADMIN_PASSWORD);
    }

    private ProductionCredentialsGuard guardWith(String environment, String datasourceUsername,
            String datasourcePassword, String actuatorUsername, String actuatorPassword,
            String adminUsername, String adminPassword) {
        ProductionCredentialsGuard guard = new ProductionCredentialsGuard();
        ReflectionTestUtils.setField(guard, "environment", environment);
        ReflectionTestUtils.setField(guard, "datasourceUsername", datasourceUsername);
        ReflectionTestUtils.setField(guard, "datasourcePassword", datasourcePassword);
        ReflectionTestUtils.setField(guard, "actuatorUsername", actuatorUsername);
        ReflectionTestUtils.setField(guard, "actuatorPassword", actuatorPassword);
        ReflectionTestUtils.setField(guard, "adminUsername", adminUsername);
        ReflectionTestUtils.setField(guard, "adminPassword", adminPassword);
        return guard;
    }

    @Test
    @DisplayName("Should reject default datasource password in production")
    void shouldRejectDefaultDatasourcePasswordInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME, "admin",
                SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME, SAFE_ADMIN_PASSWORD);

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password");
    }

    @Test
    @DisplayName("Should reject default actuator credentials in production")
    void shouldRejectDefaultActuatorCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME,
                SAFE_DATASOURCE_PASSWORD, "admin", "admin", SAFE_ADMIN_USERNAME, SAFE_ADMIN_PASSWORD);

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("actuator.security.username")
                .hasMessageContaining("actuator.security.password");
    }

    @Test
    @DisplayName("Should reject default seeded admin credentials in production (issue #651)")
    void shouldRejectDefaultAdminSeedCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME,
                SAFE_DATASOURCE_PASSWORD, SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, "admin", "admin");

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.admin.username")
                .hasMessageContaining("app.admin.password");
    }

    @Test
    @DisplayName("Should accept non-default credentials in production")
    void shouldAcceptNonDefaultCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production");

        assertThatCode(guard::validateCredentials).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should not validate credentials outside production")
    void shouldSkipValidationOutsideProduction() {
        ProductionCredentialsGuard guard = guardWith("development", "admin", "admin", "admin", "admin",
                "admin", "admin");

        assertThatCode(guard::validateCredentials).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should treat environment check as case-insensitive")
    void shouldTreatEnvironmentAsCaseInsensitive() {
        ProductionCredentialsGuard guard = guardWith("PRODUCTION", SAFE_DATASOURCE_USERNAME, "admin",
                SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME, SAFE_ADMIN_PASSWORD);

        assertThatThrownBy(guard::validateCredentials).isInstanceOf(IllegalStateException.class);
    }
}
