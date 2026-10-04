package com.licensis.notaire.dto;

/**
 * A testimony of a gestión's escritura with the state of its registry circuit
 * (SIN_INGRESAR, INGRESADO, INSCRIPTO or RETIRADO) and the number of copias issued.
 */
public record DtoCaseTestimony(
        Integer idTestimony,
        int number,
        boolean verified,
        boolean flagged,
        String state,
        int copies) {
}
