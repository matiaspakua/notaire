package com.licensis.notaire.exception;

import com.licensis.notaire.business.ProcedureFolder;
import java.util.List;

/**
 * Thrown by {@code ManagementArchiveDebtService.archiving} when a management has
 * procedure folders still in "Espera" and archiving was not explicitly
 * confirmed (CU85, CU16 — exception 5.1). Intentionally not a
 * {@link NotaireException} subclass: {@code ManagementController}'s local
 * try/catch maps this directly to HTTP 409 with the pending folders.
 */
public class FoldersInWaitException extends RuntimeException {

    private final transient List<ProcedureFolder> foldersInWait;

    public FoldersInWaitException(String message, List<ProcedureFolder> foldersInWait) {
        super(message);
        this.foldersInWait = foldersInWait;
    }

    public List<ProcedureFolder> getFoldersInWait() {
        return foldersInWait;
    }
}
