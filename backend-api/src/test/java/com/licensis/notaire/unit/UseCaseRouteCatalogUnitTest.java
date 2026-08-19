package com.licensis.notaire.unit;

import com.licensis.notaire.testing.UseCaseRouteCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UseCaseRouteCatalogUnitTest {

    @Test
    void shouldContainAllRoutesWithoutDuplicates() {
        List<UseCaseRouteCatalog.UseCaseRoute> routes = UseCaseRouteCatalog.all();
        assertEquals(72, routes.size(), "The catalog must contain all 72 routes.");

        Set<String> routeKeys = routes.stream()
                .map(route -> route.useCaseId() + " " + route.method() + " " + route.pathPattern())
                .collect(Collectors.toSet());
        assertEquals(72, routeKeys.size(),
                "Each use case route (ID + method + path) must be unique; "
                        + "one use case may legitimately cover several routes (e.g. CU83).");
    }

    @Test
    void shouldDefineRouteDataForEveryUseCase() {
        for (UseCaseRouteCatalog.UseCaseRoute route : UseCaseRouteCatalog.all()) {
            assertNotNull(route.useCaseId());
            assertNotNull(route.useCaseName());
            assertNotNull(route.method());
            assertNotNull(route.pathPattern());
            assertFalse(route.pathPattern().isBlank(), "Route path pattern must not be blank.");
        }
    }
}
