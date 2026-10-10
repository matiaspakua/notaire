package com.licensis.notaire.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.licensis.notaire.service.JpaAdministrator;

@Configuration
public class JpaConfig {

    @Autowired
    public void configureLegacyJpa(EntityManagerFactory emf) {
        JpaAdministrator.setEmf(emf);
    }
}
