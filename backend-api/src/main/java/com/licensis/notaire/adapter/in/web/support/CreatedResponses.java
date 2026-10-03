package com.licensis.notaire.adapter.in.web.support;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

/**
 * Shared helper for resource-create responses: HTTP 201 + {@code Location}.
 *
 * <p>ADR-023 / issue #1065 — controllers MUST NOT build Location URIs ad hoc.
 */
public final class CreatedResponses {

    private CreatedResponses() {
    }

    /**
     * Builds {@code 201 Created} with {@code Location: {collectionPath}/{id}}.
     *
     * @param body           response body
     * @param collectionPath absolute API path of the collection (e.g. {@code /api/v1/pagos})
     * @param id             created resource identifier
     */
    public static <T> ResponseEntity<T> of(T body, String collectionPath, Object id) {
        Objects.requireNonNull(collectionPath, "collectionPath");
        Objects.requireNonNull(id, "id");
        URI location = UriComponentsBuilder.fromPath(collectionPath)
                .pathSegment(String.valueOf(id))
                .build()
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
