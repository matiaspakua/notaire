package com.licensis.notaire.exception;

import com.licensis.notaire.negocio.CarpetaTramite;
import java.util.List;

/**
 * Thrown by {@code GestionArchiveDebtService.archivar} when a gestión has
 * carpetas de trámite still in "Espera" and archiving was not explicitly
 * confirmed (CU85, CU16 — Excepción 5.1). Intentionally NOT a
 * {@link NotaireException} subclass: {@code GestionController}'s local
 * try/catch maps this directly to HTTP 409 with the pending carpetas.
 */
public class CarpetasEnEsperaException extends RuntimeException {

    private final transient List<CarpetaTramite> carpetasEnEspera;

    public CarpetasEnEsperaException(String message, List<CarpetaTramite> carpetasEnEspera) {
        super(message);
        this.carpetasEnEspera = carpetasEnEspera;
    }

    public List<CarpetaTramite> getCarpetasEnEspera() {
        return carpetasEnEspera;
    }
}
