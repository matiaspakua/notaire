package com.licensis.notaire.service;

import com.licensis.notaire.exception.CarpetasEnEsperaException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.CarpetaTramiteRepository;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import com.licensis.notaire.repository.TramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU16 - Archivar Gestión: calcula el saldo pendiente agregado de una gestión
 * (RF-22) y persiste si quedó archivada con deuda (RF-37). Delega la
 * validación de la transición de estado (y su registro en bitácora) a
 * {@link GestionTransitionService} (CU83, CU13).
 */
@Service
public class GestionArchiveDebtService {

    private static final String ESTADO_ARCHIVADA = "Archivada";
    private static final String ESTADO_ESPERA = "Espera";

    private static final Logger log = LoggerFactory.getLogger(GestionArchiveDebtService.class);

    private final GestionDeEscrituraRepository gestionRepository;
    private final TramiteRepository tramiteRepository;
    private final PagoService pagoService;
    private final GestionTransitionService gestionTransitionService;
    private final CarpetaTramiteRepository carpetaTramiteRepository;

    public GestionArchiveDebtService(GestionDeEscrituraRepository gestionRepository,
            TramiteRepository tramiteRepository, PagoService pagoService,
            GestionTransitionService gestionTransitionService,
            CarpetaTramiteRepository carpetaTramiteRepository) {
        this.gestionRepository = gestionRepository;
        this.tramiteRepository = tramiteRepository;
        this.pagoService = pagoService;
        this.gestionTransitionService = gestionTransitionService;
        this.carpetaTramiteRepository = carpetaTramiteRepository;
    }

    /**
     * Suma el saldo pendiente de cada presupuesto vinculado a los trámites de la gestión.
     */
    @Transactional(readOnly = true)
    public Float calcularSaldoPendiente(Integer idGestion) {
        gestionRepository.findById(idGestion)
                .orElseThrow(() -> new IllegalArgumentException("Gestión no encontrada con ID: " + idGestion));

        List<Tramite> tramites = tramiteRepository.findByFkIdGestionIdGestion(idGestion);
        Set<Integer> idsPresupuestoContados = new HashSet<>();
        float saldo = 0f;
        for (Tramite tramite : tramites) {
            Presupuesto presupuesto = tramite.getFkIdPresupuesto();
            if (presupuesto == null || !idsPresupuestoContados.add(presupuesto.getIdPresupuesto())) {
                continue;
            }
            Float saldoPresupuesto = pagoService.calcularSaldoPendiente(presupuesto.getIdPresupuesto());
            saldo += saldoPresupuesto != null ? saldoPresupuesto : 0f;
        }

        log.debug("Saldo pendiente agregado para gestión {}: {}", idGestion, saldo);
        return saldo;
    }

    /**
     * Resultado de archivar una gestión: la gestión ya archivada junto con el
     * saldo pendiente agregado calculado en el momento del archivado.
     */
    public record ArchiveResult(GestionDeEscritura gestion, Float saldoPendiente) { }

    /**
     * CU16 - Archiva la gestión sin exigir confirmación de carpetas en espera.
     * Equivalente a {@code archivar(idGestion, false)}.
     */
    public ArchiveResult archivar(Integer idGestion) {
        return archivar(idGestion, false);
    }

    /**
     * CU16 - Archiva la gestión, cambia todas sus carpetas de trámite a
     * "Archivada" (CU85) y registra si quedó con deuda pendiente (RF-22,
     * RF-37). El archivado no se bloquea por la existencia de deuda: la
     * advertencia de saldo pendiente se muestra al usuario antes de confirmar
     * (ver GET /saldo-pendiente), pero la confirmación del archivado siempre
     * se persiste con o sin deuda. Si alguna carpeta sigue en "Espera" y
     * {@code confirmado} es falso, se rechaza el archivado con
     * {@link CarpetasEnEsperaException} (CU85 — Excepción 5.1).
     */
    @Transactional
    public ArchiveResult archivar(Integer idGestion, boolean confirmado) {
        List<CarpetaTramite> carpetasEnEspera =
                carpetaTramiteRepository.findByFkIdGestionIdGestionAndEstado(idGestion, ESTADO_ESPERA);
        if (!carpetasEnEspera.isEmpty() && !confirmado) {
            throw new CarpetasEnEsperaException(
                    "La gestión tiene carpetas de trámite en espera sin resolver; "
                            + "confirme explícitamente para archivar de todos modos",
                    carpetasEnEspera);
        }

        Float saldoPendiente = calcularSaldoPendiente(idGestion);

        GestionDeEscritura gestion = gestionTransitionService.transicionar(idGestion, ESTADO_ARCHIVADA);
        gestion.setDeudaPendienteAlArchivar(saldoPendiente != null && saldoPendiente > 0);
        GestionDeEscritura archivedGestion = gestionRepository.save(gestion);

        archivarCarpetas(idGestion);

        log.info("Gestión {} archivada con deudaPendienteAlArchivar={}", idGestion,
                archivedGestion.getDeudaPendienteAlArchivar());
        return new ArchiveResult(archivedGestion, saldoPendiente);
    }

    private void archivarCarpetas(Integer idGestion) {
        List<CarpetaTramite> carpetas = carpetaTramiteRepository.findByFkIdGestionIdGestion(idGestion);
        carpetas.forEach(carpeta -> carpeta.setEstado(ESTADO_ARCHIVADA));
        carpetaTramiteRepository.saveAll(carpetas);
    }
}
