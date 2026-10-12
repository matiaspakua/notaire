package com.licensis.notaire.observability;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Product version shown by actuator {@code /info}.
 *
 * <p>Backend and frontend share the product semver. The database component is the
 * Flyway schema id, which is a different clock from that semver (#1571, CU76).
 */
public final class ProductReleaseInfo {

    private ProductReleaseInfo() {
    }

    public static Map<String, Object> section(String productVersion, String databaseSchema) {
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("backend", productVersion);
        components.put("frontend", productVersion);
        components.put("databaseSchema", databaseSchema);

        Map<String, Object> notaire = new LinkedHashMap<>();
        notaire.put("version", productVersion);
        notaire.put("components", components);
        return notaire;
    }
}
