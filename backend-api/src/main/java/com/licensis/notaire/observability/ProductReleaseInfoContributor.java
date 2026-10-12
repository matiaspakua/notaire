package com.licensis.notaire.observability;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * Publishes the product version and the current Flyway schema on {@code /actuator/info}.
 *
 * <p>The H2 profile turns Flyway off, so the bean is optional. A missing schema reads as
 * {@code none} instead of stopping the application context.
 */
@Component
public class ProductReleaseInfoContributor implements InfoContributor {

    private final String productVersion;
    private final Flyway flyway;

    @Autowired
    public ProductReleaseInfoContributor(
            @Value("${app.version}") String productVersion, ObjectProvider<Flyway> flyway) {
        this(productVersion, flyway.getIfAvailable());
    }

    /** Test seam. Production wiring uses {@link ObjectProvider} so a missing Flyway bean is allowed. */
    public ProductReleaseInfoContributor(String productVersion, Flyway flyway) {
        this.productVersion = productVersion;
        this.flyway = flyway;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("notaire", ProductReleaseInfo.section(productVersion, schemaVersion(flyway)));
    }

    static String schemaVersion(Flyway flyway) {
        if (flyway == null) {
            return "none";
        }
        MigrationInfo current = flyway.info().current();
        if (current == null || current.getVersion() == null) {
            return "none";
        }
        return current.getVersion().getVersion();
    }
}
