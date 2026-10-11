package com.licensis.notaire.dto;

import java.math.BigDecimal;

import java.util.Date;

/**
 * CU10 — Read model for one external-entity document on a management. Exposes
 * the fields the use case's main flow (step 4) requires: name, prepared flag,
 * card number, entry date, exit date, flagged flag, amount due, payment date,
 * release date, notes, and whether it was delivered.
 */
public record DtoExternalEntityDocument(
        Integer idSubmittedDocument,
        String name,
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
