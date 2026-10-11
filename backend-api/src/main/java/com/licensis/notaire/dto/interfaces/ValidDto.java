package com.licensis.notaire.dto.interfaces;

/**
 * Contract for a DTO that can report whether its own state is valid.
 * Each DTO may override {@code isValid()} for its own rules.
 *
 * @author matias
 */
public interface ValidDto {

    /**
     * Id assigned when a DTO is first created (id = -1).
     */
    int ID_DTO_INICIALIZADO = -1;
    int INITIAL_VERSION = 0;

    /**
     * Reports whether this DTO is valid.
     * The default implementation returns true. DTOs may override it with their own checks.
     *
     * @return true when the DTO state is valid
     */
    default Boolean isValid() {
        return true;
    }
}
