package com.licensis.notaire.application.port.out.document;

/** Whether a document type is referenced by procedure templates or submitted documents. */
public interface DocumentTypeUsagePort {

    boolean isInUse(int id);
}
