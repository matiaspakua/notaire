package com.licensis.notaire.business;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Resolves identification-type names and ids from the catalog. Entities are not Spring beans, so they reach the
 * default instance through {@link #shared()}. The catalog supplier is wired from the legacy JPA side
 * ({@code JpaAdministrator#setEmf}) so this class stays free of {@code jpa.*} imports (ArchUnit R6 / #1914).
 */
public final class IdentificationTypeLookup {

    private static Supplier<List<IdentificationType>> catalogSupplier = List::of;

    private static final IdentificationTypeLookup SHARED = new IdentificationTypeLookup(() -> catalogSupplier.get());

    private final Supplier<List<IdentificationType>> catalog;

    public IdentificationTypeLookup(Supplier<List<IdentificationType>> catalog) {
        this.catalog = catalog;
    }

    /**
     * Installs the live catalog supplier used by {@link #shared()}. Called once when the legacy EMF is set.
     */
    public static void wireCatalog(Supplier<List<IdentificationType>> catalog) {
        catalogSupplier = Objects.requireNonNull(catalog, "catalog");
    }

    public static IdentificationTypeLookup shared() {
        return SHARED;
    }

    public String nameOf(Integer identificationTypeId) {
        if (identificationTypeId == null) {
            return null;
        }
        return catalog.get().stream()
                .filter(type -> identificationTypeId.equals(type.getIdIdentificationType()))
                .map(IdentificationType::getName)
                .findFirst()
                .orElse(null);
    }

    public int idOf(String identificationTypeName) {
        if (identificationTypeName == null) {
            return 0;
        }
        return catalog.get().stream()
                .filter(type -> identificationTypeName.equalsIgnoreCase(type.getName()))
                .map(IdentificationType::getIdIdentificationType)
                .findFirst()
                .orElse(0);
    }
}
