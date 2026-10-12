package com.licensis.notaire.observability;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Publishes the product version and the current Flyway schema on {@code /actuator/info}.
 */
@Component
public class ProductReleaseInfoContributor implements InfoContributor {

    private final String productVersion;
    private final Flyway flyway;

    public ProductReleaseInfoContributor(@Value("${app.version}") String productVersion, Flyway flyway) {
        this.productVersion = productVersion;
        this.flyway = flyway;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("notaire", ProductReleaseInfo.section(productVersion, schemaVersion(flyway)));
    }

    static String schemaVersion(Flyway flyway) {
        MigrationInfo current = flyway.info().current();
        if (current == null || current.getVersion() == null) {
            return "none";
        }
        return current.getVersion().getVersion();
    }
}
