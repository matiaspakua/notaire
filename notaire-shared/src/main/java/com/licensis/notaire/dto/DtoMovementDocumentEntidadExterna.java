package com.licensis.notaire.dto;

import java.util.Date;

/**
 * CU10 - Request body for registering the movement data of a single
 * "Entidad Externa" document (paso 5 del Curso de Eventos).
 */
public record DtoMovementDocumentEntidadExterna(
        Boolean prepared,
        Integer cardNumber,
        Date dateEntry,
        Date dateExit,
        Boolean flagged,
        Float amountToPay,
        Date datePayment,
        Date dateReleased,
        String notes,
        Boolean delivered) {
}
