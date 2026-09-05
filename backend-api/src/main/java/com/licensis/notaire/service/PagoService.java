package com.licensis.notaire.service;

import com.licensis.notaire.dto.TipoItem;
import com.licensis.notaire.exception.SaldoPendienteExcedidoException;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PagoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    private final PagoRepository pagoRepository;
    private final PresupuestoRepository presupuestoRepository;

    public PagoService(PagoRepository pagoRepository, PresupuestoRepository presupuestoRepository) {
        this.pagoRepository = pagoRepository;
        this.presupuestoRepository = presupuestoRepository;
    }

    /**
     * CU15 - Procesar pago: Registra un nuevo pago para un presupuesto.
     * Calcula el saldo pendiente y valida que el monto no exceda el total.
     */
    @Transactional
    public Pago procesarPago(Integer idPresupuesto, Float monto, Date fecha, String observaciones) {
        return procesarPago(idPresupuesto, monto, fecha, observaciones, null);
    }

    /**
     * CU15 - Procesar pago: Registra un nuevo pago para un presupuesto.
     * Calcula el saldo pendiente y valida que el monto no exceda el total.
     */
    @Transactional
    public Pago procesarPago(Integer idPresupuesto, Float monto, Date fecha, String observaciones,
            String metodoPago) {
        log.info("Procesando pago para presupuesto {}: monto={}", idPresupuesto, monto);

        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idPresupuesto));

        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }

        Float saldoPendiente = calcularSaldoPendiente(idPresupuesto);
        log.info("Saldo pendiente para presupuesto {}: {}", idPresupuesto, saldoPendiente);

        if (monto > saldoPendiente) {
            throw new SaldoPendienteExcedidoException(
                    String.format("El monto del pago ($%.2f) no puede exceder el saldo pendiente ($%.2f)",
                            monto, saldoPendiente));
        }

        Pago pago = new Pago();
        pago.setMonto(monto);
        pago.setFecha(fecha != null ? fecha : new Date());
        pago.setObservaciones(observaciones);
        pago.setMetodoPago(metodoPago);
        pago.setPresupuesto(presupuesto);

        Pago savedPago = pagoRepository.save(pago);
        log.info("Pago registrado exitosamente: ID={}", savedPago.getIdPago());

        return savedPago;
    }

    /**
     * CU47 - Consultar Pago: Obtiene un pago por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Pago> consultarPago(Integer idPago) {
        log.debug("Consultando pago con ID: {}", idPago);
        return pagoRepository.findById(idPago);
    }

    /**
     * Obtiene todos los pagos de un presupuesto.
     */
    @Transactional(readOnly = true)
    public List<Pago> findPagosByPresupuesto(Integer idPresupuesto) {
        return pagoRepository.findByFkIdPresupuestoIdPresupuesto(idPresupuesto);
    }

    /**
     * Calcula el saldo pendiente de un presupuesto.
     */
    @Transactional(readOnly = true)
    public Float calcularSaldoPendiente(Integer idPresupuesto) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idPresupuesto));

        Float totalPresupuesto = calcularTotalPresupuesto(presupuesto);
        Float totalPagado = pagoRepository.sumMontoByPresupuestoId(idPresupuesto);
        Float saldoPendiente = totalPresupuesto - (totalPagado != null ? totalPagado : 0f);

        log.debug("Saldo pendiente para presupuesto {}: {}", idPresupuesto, saldoPendiente);
        return saldoPendiente;
    }

    /**
     * CU15/CU47 - Calcula el estado de pago agregado de un presupuesto (Issue #821):
     * SIN_PAGOS si no se registró ningún pago, SALDADO si el saldo pendiente es cero,
     * PARCIAL en cualquier otro caso.
     */
    @Transactional(readOnly = true)
    public EstadoPago calcularEstadoPago(Integer idPresupuesto) {
        presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto no encontrado con ID: " + idPresupuesto));

        Float totalPagado = pagoRepository.sumMontoByPresupuestoId(idPresupuesto);
        if (totalPagado == null || totalPagado == 0f) {
            return EstadoPago.SIN_PAGOS;
        }

        Float saldoPendiente = calcularSaldoPendiente(idPresupuesto);
        return saldoPendiente <= 0f ? EstadoPago.SALDADO : EstadoPago.PARCIAL;
    }

    /**
     * CU45 - Calcula el total de un presupuesto sumando los items normales y de recargo,
     * restando los items de descuento, y sumando los costos de documentos presentados
     * en sus trámites (Issue #823).
     */
    private Float calcularTotalPresupuesto(Presupuesto presupuesto) {
        float total;
        if (presupuesto.getItemList() == null || presupuesto.getItemList().isEmpty()) {
            total = presupuesto.getMontoInmueble() != null ? presupuesto.getMontoInmueble() : 0f;
        } else {
            total = 0f;
            for (Item item : presupuesto.getItemList()) {
                total += item.getTipo() == TipoItem.DESCUENTO ? -item.getValor() : item.getValor();
                if (item.getPorcentaje() != null && item.getPorcentaje() > 0) {
                    total += total * (item.getPorcentaje() / 100.0f);
                }
            }
        }
        return total + sumarCostosDocumentosPresentados(presupuesto);
    }

    private float sumarCostosDocumentosPresentados(Presupuesto presupuesto) {
        if (presupuesto.getTramiteList() == null) {
            return 0f;
        }
        float total = 0f;
        for (var tramite : presupuesto.getTramiteList()) {
            if (tramite.getDocumentoPresentadoList() == null) {
                continue;
            }
            for (var documento : tramite.getDocumentoPresentadoList()) {
                if (documento.getImporteAPagar() != null) {
                    total += documento.getImporteAPagar();
                }
            }
        }
        return total;
    }

    /**
     * Obtiene todos los pagos.
     */
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }

    /**
     * Obtiene pagos en un rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<Pago> findPagosByFechaRange(Date startDate, Date endDate) {
        return pagoRepository.findByFechaBetween(startDate, endDate);
    }

    /**
     * Elimina un pago por su ID.
     */
    @Transactional
    public void deletePago(Integer idPago) {
        log.info("Eliminando pago con ID: {}", idPago);
        if (!pagoRepository.existsById(idPago)) {
            throw new IllegalArgumentException("Pago no encontrado con ID: " + idPago);
        }
        pagoRepository.deleteById(idPago);
        log.info("Pago eliminado exitosamente: ID={}", idPago);
    }

    @Transactional
    public Pago editarPago(Integer idPago, Float monto, Date fecha, String observaciones) {
        return editarPago(idPago, monto, fecha, observaciones, null);
    }

    /**
     * Edita un pago existente.
     */
    @Transactional
    public Pago editarPago(Integer idPago, Float monto, Date fecha, String observaciones, String metodoPago) {
        log.info("Editando pago con ID: {}", idPago);
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado con ID: " + idPago));

        if (monto != null) {
            if (monto <= 0) {
                throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
            }
            pago.setMonto(monto);
        }
        if (fecha != null) {
            pago.setFecha(fecha);
        }
        if (observaciones != null) {
            pago.setObservaciones(observaciones);
        }
        if (metodoPago != null) {
            pago.setMetodoPago(metodoPago);
        }

        Pago savedPago = pagoRepository.save(pago);
        log.info("Pago editado exitosamente: ID={}", savedPago.getIdPago());
        return savedPago;
    }
}
