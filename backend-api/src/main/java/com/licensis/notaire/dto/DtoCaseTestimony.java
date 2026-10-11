package com.licensis.notaire.dto;

/**
 * A testimony of a management's deed with the state of its registry circuit
 * (SIN_INGRESAR, INGRESADO, INSCRIPTO or RETIRADO) and the number of copies issued.
 */
public record DtoCaseTestimony(
        Integer idTestimony,
        int number,
        boolean verified,
        boolean flagged,
        String state,
        int copies) {
}
