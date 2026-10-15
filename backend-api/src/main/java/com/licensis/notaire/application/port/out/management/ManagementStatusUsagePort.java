package com.licensis.notaire.application.port.out.management;

/** Whether a management status is referenced by deed managements. */
public interface ManagementStatusUsagePort {

    boolean isUsedByDeedManagements(int id);
}
