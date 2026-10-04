package com.licensis.notaire.dto;

import java.util.List;

/** An escritura reached through a gestión's trámites, with its testimonios. */
public record DtoCaseDeed(
        Integer idDeed,
        int number,
        String status,
        List<DtoCaseTestimony> testimonies) {
}
