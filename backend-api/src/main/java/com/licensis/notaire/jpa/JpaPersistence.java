package com.licensis.notaire.jpa;

/**
 * Implemented by every JPA controller. {@link #getNameJpa()} returns the controller's
 * class name so the administrator can keep one shared instance of each controller.
 *
 * @author matias
 */
public interface JpaPersistence
{

    /**
     * Returns the class name of this JPA controller.
     *
     * @return the controller class name
     */
    String getNameJpa();
}
