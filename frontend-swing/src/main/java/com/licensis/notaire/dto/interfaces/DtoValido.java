package com.licensis.notaire.dto.interfaces;

/**
 * Interfaz para DTOs. Versión frontend sin dependencias de validación backend.
 */
public interface DtoValido {
    int ID_DTO_INICIALIZADO = -1;
    int VERSION_INICIAL = 0;
    default Boolean isValido() { return true; }
}
