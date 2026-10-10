package com.licensis.notaire.dto;

import java.util.List;

/** An deed reached through a management's procedures, with its testimonies. */
public record DtoCaseDeed(
        Integer idDeed,
        int number,
        String status,
        List<DtoCaseTestimony> testimonies) {
}
