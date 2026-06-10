package com.licensis.notaire.service;

import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.PresupuestoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service layer for Presupuesto domain operations.
 * Replaces the legacy PresupuestoJpaController access pattern.
 */
@Service
@Transactional
public class PresupuestoService {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoService.class);

    private final PresupuestoRepository presupuestoRepository;

    public PresupuestoService(PresupuestoRepository presupuestoRepository) {
        this.presupuestoRepository = presupuestoRepository;
    }

    /**
     * Returns all presupuestos.
     *
     * @return list of all presupuestos
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findAll() {
        log.debug("Finding all presupuestos");
        return presupuestoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Presupuesto> findAllPaged(Pageable pageable) {
        return presupuestoRepository.findAll(pageable);
    }

    /**
     * Finds a presupuesto by its primary key.
     *
     * @param id the presupuesto ID
     * @return optional containing the presupuesto, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Presupuesto> findById(Integer id) {
        log.debug("Finding presupuesto by id: {}", id);
        return presupuestoRepository.findById(id);
    }

    /**
     * Returns all presupuestos belonging to a given persona (CU60).
     *
     * @param idPersona the persona ID
     * @return list of presupuestos for the persona
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findByPersona(Integer idPersona) {
        log.debug("Finding presupuestos for persona id: {}", idPersona);
        return presupuestoRepository.findByFkIdPersonaIdPersona(idPersona);
    }

    /**
     * Filters presupuestos by estado (CU60).
     * If estado is blank or null, all presupuestos are returned.
     *
     * @param estado the estado value to filter by (may be null or blank)
     * @return matching presupuestos
     */
    @Transactional(readOnly = true)
    public List<Presupuesto> findByEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            log.debug("Returning all presupuestos (no estado filter)");
            return presupuestoRepository.findAll();
        }
        log.debug("Finding presupuestos by estado: {}", estado);
        return presupuestoRepository.findByEstado(estado);
    }

    /**
     * Persists a new presupuesto. Applies default values for required fields when absent.
     *
     * @param presupuesto the entity to persist
     * @return the saved presupuesto with generated ID
     */
    public Presupuesto create(Presupuesto presupuesto) {
        if (presupuesto.getEncabezado() == null || presupuesto.getEncabezado().isBlank()) {
            presupuesto.setEncabezado("Presupuesto");
        }
        if (presupuesto.getNumero() == 0) {
            presupuesto.setNumero((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        }
        log.info("Creating presupuesto: numero={}", presupuesto.getNumero());
        return presupuestoRepository.save(presupuesto);
    }

    /**
     * Updates an existing presupuesto.
     *
     * @param id          the presupuesto ID
     * @param presupuesto the updated state
     * @return the saved presupuesto
     * @throws ResourceNotFoundException if no presupuesto with the given ID exists
     */
    public Presupuesto update(Integer id, Presupuesto presupuesto) {
        if (!presupuestoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        presupuesto.setIdPresupuesto(id);
        log.info("Updating presupuesto id: {}", id);
        return presupuestoRepository.save(presupuesto);
    }

    /**
     * Deletes a presupuesto by ID.
     *
     * @param id the presupuesto ID
     * @throws ResourceNotFoundException if no presupuesto with the given ID exists
     */
    public void deleteById(Integer id) {
        if (!presupuestoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id);
        }
        log.info("Deleting presupuesto id: {}", id);
        presupuestoRepository.deleteById(id);
    }
}
