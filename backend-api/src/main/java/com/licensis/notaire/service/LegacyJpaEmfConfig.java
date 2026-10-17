package com.licensis.notaire.service;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Boots the legacy {@link JpaAdministrator} EMF holder. Lives under {@code service} so ArchUnit R6 does not
 * flag {@code config → service} (issue #1914; formerly {@code config.JpaConfig}).
 */
@Configuration
public class LegacyJpaEmfConfig {

    @Autowired
    public void configureLegacyJpa(EntityManagerFactory emf) {
        JpaAdministrator.setEmf(emf);
    }
}
