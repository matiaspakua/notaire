package com.licensis.notaire.domain.management;

/** The client edited a version that is no longer the stored one. */
public class ManagementStatusStaleVersionException extends ManagementStatusException {

    public ManagementStatusStaleVersionException(int id, int stored, int received) {
        super("Management status " + id + " is at version " + stored + ", not " + received);
    }
}
