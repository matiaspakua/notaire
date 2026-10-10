package com.licensis.notaire.dto;

import java.math.BigDecimal;

import java.util.Date;

/**
 * CU10 - Read-model for a single "Entidad Externa" document tracked within a
 * management, exposing exactly the fields the use case's Curso of Eventos (step 4)
 * requires: name, preparado, number of cartón, date of ingreso, date of
 * salida, observado, monto debt, date of payment, date of liberación,
 * observaciones y si fue finalizado (entregado).
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
