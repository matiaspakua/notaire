package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.business.DeedManagement;

public record ManagementRef(Integer idManagement, int number, String heading) {

    public static ManagementRef from(DeedManagement management) {
        return management == null ? null
                : new ManagementRef(management.getIdManagement(), management.getNumber(), management.getHeading());
    }
}
