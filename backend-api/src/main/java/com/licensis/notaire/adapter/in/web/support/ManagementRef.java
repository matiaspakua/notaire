package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.DeedManagement;

public record ManagementRef(Integer idManagement, int number, String encabezado) {

    public static ManagementRef from(DeedManagement management) {
        return management == null ? null
                : new ManagementRef(management.getIdManagement(), management.getNumber(), management.getEncabezado());
    }
}
