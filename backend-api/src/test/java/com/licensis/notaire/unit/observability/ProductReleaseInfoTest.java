package com.licensis.notaire.unit.observability;

import com.licensis.notaire.observability.ProductReleaseInfo;
import com.licensis.notaire.observability.ProductReleaseInfoContributor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Product release info (issue #1571)")
class ProductReleaseInfoTest {

    @Test
    @DisplayName("Should publish backend, frontend, and database schema as one product section")
    void shouldPublishThreeComponents() {
        Map<String, Object> section = ProductReleaseInfo.section("0.1.2", "46");

        assertThat(section).containsEntry("version", "0.1.2");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) section.get("components");
        assertThat(components)
                .containsEntry("backend", "0.1.2")
                .containsEntry("frontend", "0.1.2")
                .containsEntry("databaseSchema", "46");
        assertThat(components.get("databaseSchema")).isNotEqualTo(section.get("version"));
    }

    @Test
    @DisplayName("Should read the current Flyway version onto actuator info")
    void shouldContributeSchemaFromFlyway() {
        Flyway flyway = mock(Flyway.class);
        MigrationInfoService migrations = mock(MigrationInfoService.class);
        MigrationInfo current = mock(MigrationInfo.class);
        when(flyway.info()).thenReturn(migrations);
        when(migrations.current()).thenReturn(current);
        when(current.getVersion()).thenReturn(MigrationVersion.fromVersion("46"));

        ProductReleaseInfoContributor contributor = new ProductReleaseInfoContributor("0.1.2", flyway);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);

        @SuppressWarnings("unchecked")
        Map<String, Object> notaire = (Map<String, Object>) builder.build().getDetails().get("notaire");
        assertThat(notaire).containsEntry("version", "0.1.2");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) notaire.get("components");
        assertThat(components).containsEntry("databaseSchema", "46");
    }

    @Test
    @DisplayName("Should report none when Flyway has not applied a version yet")
    void shouldReportNoneWhenSchemaIsAbsent() {
        Flyway flyway = mock(Flyway.class);
        MigrationInfoService migrations = mock(MigrationInfoService.class);
        when(flyway.info()).thenReturn(migrations);
        when(migrations.current()).thenReturn(null);

        ProductReleaseInfoContributor contributor = new ProductReleaseInfoContributor("0.1.2", flyway);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);

        @SuppressWarnings("unchecked")
        Map<String, Object> notaire = (Map<String, Object>) builder.build().getDetails().get("notaire");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) notaire.get("components");
        assertThat(components).containsEntry("databaseSchema", "none");
    }

    @Test
    @DisplayName("Should report none when the Flyway bean is absent")
    void shouldReportNoneWhenFlywayBeanIsAbsent() {
        ProductReleaseInfoContributor contributor = new ProductReleaseInfoContributor("0.1.2", (Flyway) null);
        Info.Builder builder = new Info.Builder();
        contributor.contribute(builder);

        @SuppressWarnings("unchecked")
        Map<String, Object> notaire = (Map<String, Object>) builder.build().getDetails().get("notaire");
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) notaire.get("components");
        assertThat(components).containsEntry("databaseSchema", "none");
    }
}
