package com.licensis.notaire.service;

import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
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

    private static final Logger log = LoggerFactory.getLogger(GestionArchiveDebtService.class);

    private final GestionDeEscrituraRepository gestionRepository;
    private final TramiteRepository tramiteRepository;
    private final PagoService pagoService;
    private final GestionTransitionService gestionTransitionService;

    public GestionArchiveDebtService(GestionDeEscrituraRepository gestionRepository,
            TramiteRepository tramiteRepository, PagoService pagoService,
            GestionTransitionService gestionTransitionService) {
        this.gestionRepository = gestionRepository;
        this.tramiteRepository = tramiteRepository;
        this.pagoService = pagoService;
        this.gestionTransitionService = gestionTransitionService;
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
     * CU16 - Archiva la gestión y registra si quedó con deuda pendiente (RF-22, RF-37).
     * El archivado no se bloquea por la existencia de deuda: la advertencia de saldo
     * pendiente se muestra al usuario antes de confirmar (ver GET /saldo-pendiente),
     * pero la confirmación del archivado siempre se persiste con o sin deuda.
     */
    @Transactional
    public ArchiveResult archivar(Integer idGestion) {
        Float saldoPendiente = calcularSaldoPendiente(idGestion);

        GestionDeEscritura gestion = gestionTransitionService.transicionar(idGestion, ESTADO_ARCHIVADA);
        gestion.setDeudaPendienteAlArchivar(saldoPendiente != null && saldoPendiente > 0);
        GestionDeEscritura archivedGestion = gestionRepository.save(gestion);

        log.info("Gestión {} archivada con deudaPendienteAlArchivar={}", idGestion,
                archivedGestion.getDeudaPendienteAlArchivar());
        return new ArchiveResult(archivedGestion, saldoPendiente);
    }
}
