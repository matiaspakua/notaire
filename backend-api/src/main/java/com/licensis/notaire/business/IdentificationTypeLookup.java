package com.licensis.notaire.business;

import com.licensis.notaire.jpa.IdentificationTypeJpaController;
import java.util.List;
import java.util.function.Supplier;

/**
 * Resolves identification-type names and ids from the catalog. Entities are not Spring beans, so they reach the
 * default instance through {@link #shared()}.
 */
public final class IdentificationTypeLookup {

    private static final IdentificationTypeLookup SHARED = new IdentificationTypeLookup(
            () -> IdentificationTypeJpaController.getInstancia().findIdentificationTypeEntities());

    private final Supplier<List<IdentificationType>> catalog;

    public IdentificationTypeLookup(Supplier<List<IdentificationType>> catalog) {
        this.catalog = catalog;
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
