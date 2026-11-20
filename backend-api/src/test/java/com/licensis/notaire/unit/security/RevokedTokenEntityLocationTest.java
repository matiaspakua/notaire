package com.licensis.notaire.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.junit.jupiter.api.Test;

/**
 * The logout revocation row is not part of the shared business package (#2351).
 */
class RevokedTokenEntityLocationTest {

    @Test
    void revokedTokenLivesWithSecurityPersistence() throws Exception {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("com.licensis.notaire.business.RevokedToken"));
        Class<?> type = Class.forName("com.licensis.notaire.adapter.out.persistence.security.RevokedToken");
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals("revoked_tokens", type.getAnnotation(Table.class).name());
    }
}
