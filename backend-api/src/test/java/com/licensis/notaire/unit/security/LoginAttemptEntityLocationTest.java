package com.licensis.notaire.unit.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.junit.jupiter.api.Test;

/**
 * The lockout row is not part of the shared business package (#2351).
 */
class LoginAttemptEntityLocationTest {

    @Test
    void loginAttemptLivesWithItsStore() throws Exception {
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("com.licensis.notaire.business.LoginAttempt"));
        Class<?> type = Class.forName("com.licensis.notaire.adapter.out.persistence.security.LoginAttempt");
        assertNotNull(type.getAnnotation(Entity.class));
        assertEquals("login_attempts", type.getAnnotation(Table.class).name());
    }
}
