package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.negocio.EstadoDeGestion;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.repository.HistorialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * CU13 - Escribe y consulta la bitácora ({@link Historial}) de cambios de
 * estado de una gestión.
 */
@Service
public class GestionBitacoraService {

    private final HistorialRepository historialRepository;

    public GestionBitacoraService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    /**
     * Registra en la bitácora el estado actual de la gestión (alta, transición
     * válida o archivado).
     */
    @Transactional
    public Historial registrarEstado(GestionDeEscritura gestion, String observaciones) {
        EstadoDeGestion estadoActual = gestion.getFkIdEstadoDeGestion();
        if (estadoActual == null) {
            throw new BusinessValidationException(
                    "No se puede registrar bitácora: la gestión " + gestion.getIdGestion()
                            + " no tiene un estado asignado");
        }

        Historial historial = new Historial();
        historial.setFkIdGestion(gestion);
        historial.setFkIdEstadoGestion(estadoActual);
        historial.setFecha(new Date());
        historial.setObservaciones(observaciones);

        return historialRepository.save(historial);
    }

    /**
     * Devuelve la bitácora completa de la gestión ordenada cronológicamente.
     */
    @Transactional(readOnly = true)
    public List<Historial> obtenerHistorial(Integer idGestion) {
        List<Historial> historial = historialRepository.findByFkIdGestionIdGestion(idGestion);
        return historial.stream()
                .sorted(Comparator.comparing(Historial::getFecha))
                .toList();
    }
}
