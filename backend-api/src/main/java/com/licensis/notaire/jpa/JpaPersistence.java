package com.licensis.notaire.jpa;

/**
 * Interface that must implementar all the JPA (Java Persistence Accesor), and cuyo unico metodo is
 * getNombreJpa, the cual is utilizado by the administrador of JPA for mantener a lista of all
 * the JPA that utilizan (debido a that the instanciacion of a JPA consumo muchos recursos),and
 * search and retornar the instancia actual of a JPA.
 *
 * @author matias
 */
public interface JpaPersistence
{

    /**
     * Metodo that devuelve the name of the JPA concreto. THE name corresponde a:
     * jpaConcreto.class.getName()
     *
     * @return Retorna the name of the JPA actual.
     */
    String getNameJpa();
}
