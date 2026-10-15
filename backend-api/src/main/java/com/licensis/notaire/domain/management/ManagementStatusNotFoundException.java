package com.licensis.notaire.domain.management;

public class ManagementStatusNotFoundException extends ManagementStatusException {

    public ManagementStatusNotFoundException(int id) {
        super("Management status not found with ID: " + id);
    }
}
