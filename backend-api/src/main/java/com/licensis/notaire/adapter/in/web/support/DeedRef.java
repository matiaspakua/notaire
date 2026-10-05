package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.Deed;

public record DeedRef(Integer idDeed, int number, String status) {

    public static DeedRef from(Deed deed) {
        return deed == null ? null : new DeedRef(deed.getIdDeed(), deed.getNumber(), deed.getStatus());
    }
}
