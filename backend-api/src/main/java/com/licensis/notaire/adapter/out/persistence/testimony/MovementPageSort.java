package com.licensis.notaire.adapter.out.persistence.testimony;

import org.springframework.data.domain.Sort;

/** Whitelist for GET /api/v1/testimony-movements sort. Unknown names fall back to newest id. */
public final class MovementPageSort {

    private MovementPageSort() {
    }

    public static Sort of(String sort) {
        String value = sort == null || sort.isBlank() ? "idTestimonyMovement,desc" : sort;
        String[] parts = value.split(",", 2);
        String raw = parts[0].trim();
        boolean known = switch (raw) {
            case "idTestimonyMovement", "dateEntry", "dateExit", "dateRegistration" -> true;
            default -> false;
        };
        String property = known ? raw : "idTestimonyMovement";
        boolean ascending = known && parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim());
        return Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, property);
    }
}
