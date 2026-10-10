package com.licensis.notaire.exception;

import com.licensis.notaire.business.ProcedureFolder;
import java.util.List;

/**
 * Thrown by {@code GestionArchiveDebtService.archivar} when a management has
 * procedure folders still in "Espera" and archiving was not explicitly
 * confirmed (CU85, CU16 — Excepción 5.1). Intentionally NOT a
 * {@link NotaireException} subclass: {@code GestionController}'s local
 * try/catch maps this directly to HTTP 409 with the pending carpetas.
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
