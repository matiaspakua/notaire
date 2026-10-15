package com.licensis.notaire.application.port.in.management;

import java.util.List;

import com.licensis.notaire.domain.management.ManagementStatus;

public interface QueryManagementStatusesUseCase {

    List<ManagementStatus> findAll();

    List<ManagementStatus> searchByName(String text);

    /** @throws com.licensis.notaire.domain.management.ManagementStatusNotFoundException when absent */
    ManagementStatus getById(int id);

    /** @throws com.licensis.notaire.domain.management.ManagementStatusNotFoundException when absent */
    boolean isInUse(int id);
}
