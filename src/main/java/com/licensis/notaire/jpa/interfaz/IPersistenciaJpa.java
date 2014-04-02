/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa.interfaz;

/**
 * Interface que deben implementar todos los JPA (Java Persistence Accesor), y cuyo unico metodo es
 * getNombreJpa, el cual es utilizado por el administrador de JPA para mantener una lista de todos
 * los JPA que se utilizan (debido a que la instanciacion de un JPA consumo muchos recursos),y
 * buscar y retornar la instancia actual de un JPA.
 *
 * @author matias
 */
public interface IPersistenciaJpa
{

    /**
     * Metodo que devuelve el nombre del JPA concreto. EL nombre corresponde a:
     * jpaConcreto.class.getName()
     *
     * @return Retorna el nombre del JPA actual.
     */
    public String getNombreJpa();
}
