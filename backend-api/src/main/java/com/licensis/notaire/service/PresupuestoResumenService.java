package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoPresupuestoResumen;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.repository.TramiteRepository;
import com.licensis.notaire.service.mappers.PagoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CU47 - Consultar Pago: resumen financiero de un presupuesto (total, saldo
 * pendiente y pagos aplicados), junto con la gestión a la que pertenece.
 */
@Service
public class PresupuestoResumenService {

    private final PresupuestoRepository presupuestoRepository;
    private final TramiteRepository tramiteRepository;
    private final PagoService pagoService;

    public PresupuestoResumenService(PresupuestoRepository presupuestoRepository,
            TramiteRepository tramiteRepository, PagoService pagoService) {
        this.presupuestoRepository = presupuestoRepository;
        this.tramiteRepository = tramiteRepository;
        this.pagoService = pagoService;
    }

    @Transactional(readOnly = true)
    public DtoPresupuestoResumen obtenerResumen(Integer idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idPresupuesto));

        Float saldoPendiente = pagoService.calcularSaldoPendiente(idPresupuesto);
        var pagos = pagoService.findPagosByPresupuesto(idPresupuesto);
        float totalPagado = (float) pagos.stream().mapToDouble(p -> p.getMonto()).sum();
        Float total = saldoPendiente + totalPagado;

        List<Tramite> tramites = tramiteRepository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto);
        Tramite tramite = tramites.isEmpty() ? null : tramites.get(0);
        var gestion = tramite != null ? tramite.getFkIdGestion() : null;

        return new DtoPresupuestoResumen(
                presupuesto.getIdPresupuesto(),
                presupuesto.getNumero(),
                gestion != null ? gestion.getIdGestion() : null,
                gestion != null ? gestion.getNumero() : null,
                gestion != null ? gestion.getEncabezado() : null,
                total,
                saldoPendiente,
                pagos.stream().map(PagoMapper::toDto).toList());
    }
}
