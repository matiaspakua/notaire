package com.licensis.notaire.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clase base for acceder to the EntityManagerFactory
 */
@Component
public class JpaControllerProvider {

    private static EntityManagerFactory emf;

    @Autowired
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        JpaControllerProvider.emf = entityManagerFactory;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
