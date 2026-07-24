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
    private static final String SAFE_PGADMIN_PASSWORD = "s3cur3-pass4";
    private static final String SAFE_GRAFANA_USERNAME = "custom-grafana";
    private static final String SAFE_GRAFANA_PASSWORD = "s3cur3-pass5";
    private static final String SAFE_EXPORTER_USERNAME = "custom-exporter";
    private static final String SAFE_EXPORTER_PASSWORD = "s3cur3-pass6";

    private ProductionCredentialsGuard guardWith(String environment) {
        return guardWith(environment, SAFE_DATASOURCE_USERNAME, SAFE_DATASOURCE_PASSWORD,
                SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME, SAFE_ADMIN_PASSWORD,
                SAFE_PGADMIN_PASSWORD, SAFE_GRAFANA_USERNAME, SAFE_GRAFANA_PASSWORD,
                SAFE_EXPORTER_USERNAME, SAFE_EXPORTER_PASSWORD);
    }

    private ProductionCredentialsGuard guardWith(String environment, String datasourceUsername,
            String datasourcePassword, String actuatorUsername, String actuatorPassword,
            String adminUsername, String adminPassword) {
        return guardWith(environment, datasourceUsername, datasourcePassword, actuatorUsername, actuatorPassword,
                adminUsername, adminPassword, SAFE_PGADMIN_PASSWORD, SAFE_GRAFANA_USERNAME, SAFE_GRAFANA_PASSWORD,
                SAFE_EXPORTER_USERNAME, SAFE_EXPORTER_PASSWORD);
    }

    private ProductionCredentialsGuard guardWith(String environment, String datasourceUsername,
            String datasourcePassword, String actuatorUsername, String actuatorPassword,
            String adminUsername, String adminPassword, String pgAdminPassword, String grafanaUsername,
            String grafanaPassword) {
        return guardWith(environment, datasourceUsername, datasourcePassword, actuatorUsername, actuatorPassword,
                adminUsername, adminPassword, pgAdminPassword, grafanaUsername, grafanaPassword,
                SAFE_EXPORTER_USERNAME, SAFE_EXPORTER_PASSWORD);
    }

    private ProductionCredentialsGuard guardWith(String environment, String datasourceUsername,
            String datasourcePassword, String actuatorUsername, String actuatorPassword,
            String adminUsername, String adminPassword, String pgAdminPassword, String grafanaUsername,
            String grafanaPassword, String exporterUsername, String exporterPassword) {
        ProductionCredentialsGuard guard = new ProductionCredentialsGuard();
        ReflectionTestUtils.setField(guard, "environment", environment);
        ReflectionTestUtils.setField(guard, "datasourceUsername", datasourceUsername);
        ReflectionTestUtils.setField(guard, "datasourcePassword", datasourcePassword);
        ReflectionTestUtils.setField(guard, "actuatorUsername", actuatorUsername);
        ReflectionTestUtils.setField(guard, "actuatorPassword", actuatorPassword);
        ReflectionTestUtils.setField(guard, "adminUsername", adminUsername);
        ReflectionTestUtils.setField(guard, "adminPassword", adminPassword);
        ReflectionTestUtils.setField(guard, "pgAdminPassword", pgAdminPassword);
        ReflectionTestUtils.setField(guard, "grafanaUsername", grafanaUsername);
        ReflectionTestUtils.setField(guard, "grafanaPassword", grafanaPassword);
        ReflectionTestUtils.setField(guard, "exporterUsername", exporterUsername);
        ReflectionTestUtils.setField(guard, "exporterPassword", exporterPassword);
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
    @DisplayName("Should reject default pgAdmin password in production (issue #672)")
    void shouldRejectDefaultPgAdminPasswordInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME,
                SAFE_DATASOURCE_PASSWORD, SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME,
                SAFE_ADMIN_PASSWORD, "admin", SAFE_GRAFANA_USERNAME, SAFE_GRAFANA_PASSWORD);

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pgadmin.admin.password");
    }

    @Test
    @DisplayName("Should reject default Grafana credentials in production (issue #672)")
    void shouldRejectDefaultGrafanaCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME,
                SAFE_DATASOURCE_PASSWORD, SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME,
                SAFE_ADMIN_PASSWORD, SAFE_PGADMIN_PASSWORD, "admin", "admin");

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("grafana.admin.username")
                .hasMessageContaining("grafana.admin.password");
    }

    @Test
    @DisplayName("Should reject default postgres-exporter credentials in production (issue #675)")
    void shouldRejectDefaultExporterCredentialsInProduction() {
        ProductionCredentialsGuard guard = guardWith("production", SAFE_DATASOURCE_USERNAME,
                SAFE_DATASOURCE_PASSWORD, SAFE_ACTUATOR_USERNAME, SAFE_ACTUATOR_PASSWORD, SAFE_ADMIN_USERNAME,
                SAFE_ADMIN_PASSWORD, SAFE_PGADMIN_PASSWORD, SAFE_GRAFANA_USERNAME, SAFE_GRAFANA_PASSWORD,
                "admin", "admin");

        assertThatThrownBy(guard::validateCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("postgres.exporter.username")
                .hasMessageContaining("postgres.exporter.password");
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
                "admin", "admin", "admin", "admin", "admin", "admin", "admin");

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
