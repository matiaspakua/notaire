package com.licensis.notaire.dto;

/**
 * Clasificación de un ítem de presupuesto (CU45, CU71).
 * {@code NORMAL} es el valor por defecto cuando un ítem no especifica tipo.
 */
public enum TipoItem {
    NORMAL,
    DESCUENTO,
    RECARGO
}
