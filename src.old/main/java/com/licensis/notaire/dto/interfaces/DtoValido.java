/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.dto.interfaces;

/**
 * Interface que define un unico metodo que todos los DTO deben implementar para validar los mismos.
 * Cada DTO debera redefinir el metodo isValido() de acuerdo al estado de cada uno en particular.
 *
 * @author matias
 */
public interface DtoValido
{

    /**
     * Representa el ID con el cual se inicializan todos los DTO (id = -1).
     */
    public int ID_DTO_INICIALIZADO = -1;
    public int VERSION_INICIAL = 0;

    /**
     * Determina si el DTO actual es valido o no, dependiendo del estado interno del mismo, o sea,
     * si el estado del objeto es valido para poder ser usado o no.
     *
     * @return Verdadero si el estado del objeto es valido, falso en caso contrario.
     */
    public Boolean isValido();
}
