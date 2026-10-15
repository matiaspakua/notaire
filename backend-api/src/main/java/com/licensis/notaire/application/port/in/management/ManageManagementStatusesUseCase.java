package com.licensis.notaire.application.port.in.management;

import com.licensis.notaire.domain.management.ManagementStatus;

public interface ManageManagementStatusesUseCase {

    /** {@code version} is the one the client read; creating ignores it, updating must match the stored one. */
    record SaveManagementStatusCommand(String name, String notes, int version) {}

    ManagementStatus create(SaveManagementStatusCommand command);

    void update(int id, SaveManagementStatusCommand command);

    void delete(int id);
}
