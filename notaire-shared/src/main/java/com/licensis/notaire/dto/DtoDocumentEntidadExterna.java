package com.licensis.notaire.dto;

import java.util.Date;

/**
 * CU10 - Read-model for a single "Entidad Externa" document tracked within a
 * gestión, exposing exactly the fields the use case's Curso de Eventos (paso 4)
 * requires: nombre, preparado, número de cartón, fecha de ingreso, fecha de
 * salida, observado, monto deuda, fecha de pago, fecha de liberación,
 * observaciones y si fue finalizado (entregado).
 */
public record DtoDocumentEntidadExterna(
        Integer idSubmittedDocument,
        String name,
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
