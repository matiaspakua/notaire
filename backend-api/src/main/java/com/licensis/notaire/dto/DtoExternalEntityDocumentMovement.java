package com.licensis.notaire.dto;

import java.math.BigDecimal;

import java.util.Date;

/**
 * CU10 - Request body for registering the movement data of a single
 * external-entity document (step 5 of the course of events).
 */
public record DtoExternalEntityDocumentMovement(
        Boolean prepared,
        Integer cardNumber,
        Date dateEntry,
        Date dateExit,
        Boolean flagged,
        BigDecimal amountToPay,
        Date datePayment,
        Date dateReleased,
        String notes,
        Boolean delivered) {
}
