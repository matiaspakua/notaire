package com.licensis.notaire.domain.management;

/** No management exists for the requested archive id. Clients receive an empty 404. */
public class ManagementArchiveNotFoundException extends ManagementArchiveException {

    public ManagementArchiveNotFoundException() {
        super("Management not found");
    }
}
