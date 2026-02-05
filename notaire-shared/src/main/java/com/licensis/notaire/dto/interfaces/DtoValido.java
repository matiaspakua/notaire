package com.licensis.notaire.dto.interfaces;

/**
 * Interfaz que define un único método que todos los DTO deben implementar para
 * validar los mismos.
 * Cada DTO deberá redefinir el método isValido() de acuerdo al estado de cada
 * uno en particular.
 *
 * @author matias
 */
public interface DtoValido {

    /**
     * Representa el ID con el cual se inicializan todos los DTO (id = -1).
     */
    int ID_DTO_INICIALIZADO = -1;
    int VERSION_INICIAL = 0;

    /**
     * Determina si el DTO actual es válido o no, dependiendo del estado interno del
     * mismo.
     * 
     * Implementación por defecto retorna true. Los DTOs pueden sobrescribir este
     * método
     * para implementar validaciones específicas.
     *
     * @return Verdadero si el estado del objeto es válido, falso en caso contrario.
     */
    default Boolean isValido() {
        return true;
    }
}
