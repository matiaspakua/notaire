package com.licensis.notaire.dto;

import java.time.LocalDate;

/** A submitted document of a gestión's trámite, with the flags that tell how far it has progressed. */
public record DtoCaseDocument(
        Integer idSubmittedDocument,
        String name,
        String typeName,
        Integer idProcedure,
        boolean prepared,
        boolean released,
        boolean observed,
        boolean delivered,
        boolean reentered,
        LocalDate dateDue) {
}
