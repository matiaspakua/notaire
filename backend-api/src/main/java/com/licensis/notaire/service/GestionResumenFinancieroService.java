package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoGestionResumenFinanciero;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.TramiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CU47/CU02 - Resumen financiero agregado de una gestión: total presupuestado,
 * total cobrado y saldo pendiente, sumados a través de los presupuestos de
 * cada trámite vinculado.
 */
@Service
public class GestionResumenFinancieroService {

    private final TramiteRepository tramiteRepository;
    private final PagoService pagoService;
    private final GestionArchiveDebtService gestionArchiveDebtService;

    public GestionResumenFinancieroService(TramiteRepository tramiteRepository, PagoService pagoService,
            GestionArchiveDebtService gestionArchiveDebtService) {
        this.tramiteRepository = tramiteRepository;
        this.pagoService = pagoService;
        this.gestionArchiveDebtService = gestionArchiveDebtService;
    }

    @Transactional(readOnly = true)
    public DtoGestionResumenFinanciero obtenerResumen(Integer idGestion) {
        Float saldoPendiente = gestionArchiveDebtService.calcularSaldoPendiente(idGestion);

        List<Tramite> tramites = tramiteRepository.findByFkIdGestionIdGestion(idGestion);
        Set<Integer> idsPresupuestoContados = new HashSet<>();
        float totalPresupuestado = 0f;
        float totalCobrado = 0f;

        for (Tramite tramite : tramites) {
            Presupuesto presupuesto = tramite.getFkIdPresupuesto();
            if (presupuesto == null || !idsPresupuestoContados.add(presupuesto.getIdPresupuesto())) {
                continue;
            }
            Float saldoPresupuesto = pagoService.calcularSaldoPendiente(presupuesto.getIdPresupuesto());
            float cobradoPresupuesto = (float) pagoService.findPagosByPresupuesto(presupuesto.getIdPresupuesto())
                    .stream().mapToDouble(p -> p.getMonto()).sum();
            totalCobrado += cobradoPresupuesto;
            totalPresupuestado += saldoPresupuesto + cobradoPresupuesto;
        }

        return new DtoGestionResumenFinanciero(idGestion, totalPresupuestado, totalCobrado, saldoPendiente);
    }
}
