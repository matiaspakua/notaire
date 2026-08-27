package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.repository.MovimientoTestimonioRepository;
import com.licensis.notaire.repository.TestimonioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * Validates and applies the testimonio movement circuit through the property registry:
 * ingreso, inscripción, retiro and reingreso (CU11, CU12, CU44).
 */
@Service
@Transactional
public class MovimientoTestimonioService {

    private static final Logger log = LoggerFactory.getLogger(MovimientoTestimonioService.class);

    private final MovimientoTestimonioRepository movimientoTestimonioRepository;
    private final TestimonioRepository testimonioRepository;

    public MovimientoTestimonioService(MovimientoTestimonioRepository movimientoTestimonioRepository,
            TestimonioRepository testimonioRepository) {
        this.movimientoTestimonioRepository = movimientoTestimonioRepository;
        this.testimonioRepository = testimonioRepository;
    }

    private MovimientoTestimonio ultimoMovimiento(Integer idTestimonio) {
        return movimientoTestimonioRepository
                .findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(idTestimonio)
                .orElse(null);
    }

    private void requireTestimonioExists(Integer idTestimonio) {
        if (!testimonioRepository.existsById(idTestimonio)) {
            throw new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimonio);
        }
    }

    /**
     * Presents a verified testimonio for inscription, registering the fecha de ingreso.
     *
     * @param idTestimonio the testimonio ID
     * @return the created movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio is not verified, or already has an open movement
     */
    public MovimientoTestimonio ingresarInscripcion(Integer idTestimonio) {
        Testimonio testimonio = testimonioRepository.findById(idTestimonio)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimonio));

        if (!testimonio.getVerificado()) {
            throw new BusinessValidationException("El testimonio debe estar verificado para ingresar a inscripción");
        }

        MovimientoTestimonio ultimo = ultimoMovimiento(idTestimonio);
        if (ultimo != null && ultimo.getFechaSalida() == null) {
            throw new BusinessValidationException("El testimonio ya está en trámite de inscripción");
        }

        MovimientoTestimonio movimiento = new MovimientoTestimonio();
        movimiento.setTestimonio(testimonio);
        movimiento.setFechaIngreso(new Date());

        log.info("Registrando ingreso a inscripción del testimonio id: {}", idTestimonio);
        return movimientoTestimonioRepository.save(movimiento);
    }

    /**
     * Marks the most recent movement of a testimonio as inscripto, registering the fecha de inscripción.
     *
     * @param idTestimonio the testimonio ID
     * @return the updated movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio has no movement with fecha de ingreso registered
     */
    public MovimientoTestimonio registrarInscripcion(Integer idTestimonio) {
        requireTestimonioExists(idTestimonio);
        MovimientoTestimonio movimiento = Optional.ofNullable(ultimoMovimiento(idTestimonio))
                .orElseThrow(() -> new BusinessValidationException(
                        "Falta el ingreso a inscripción del testimonio antes de registrar la inscripción"));

        movimiento.setInscripta(true);
        movimiento.setFechaInscripcion(new Date());

        log.info("Registrando inscripción del testimonio id: {}", idTestimonio);
        return movimientoTestimonioRepository.save(movimiento);
    }

    /**
     * Withdraws an inscripto testimonio, registering the fecha de salida and numero de cartón.
     *
     * @param idTestimonio the testimonio ID
     * @param numeroCarton the cartón number
     * @return the updated movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio is not inscripto
     */
    public MovimientoTestimonio retirar(Integer idTestimonio, int numeroCarton) {
        requireTestimonioExists(idTestimonio);
        MovimientoTestimonio movimiento = ultimoMovimiento(idTestimonio);
        if (movimiento == null || !movimiento.getInscripta()) {
            throw new BusinessValidationException("El testimonio no está inscripto");
        }

        movimiento.setFechaSalida(new Date());
        movimiento.setNumeroCarton(numeroCarton);

        log.info("Registrando retiro del testimonio id: {}, cartón: {}", idTestimonio, numeroCarton);
        return movimientoTestimonioRepository.save(movimiento);
    }

    /**
     * Re-enters a withdrawn testimonio, creating a new movement without altering the previous one.
     *
     * @param idTestimonio the testimonio ID
     * @return the newly created movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio was not previously withdrawn
     */
    public MovimientoTestimonio reingresar(Integer idTestimonio) {
        requireTestimonioExists(idTestimonio);
        MovimientoTestimonio ultimo = ultimoMovimiento(idTestimonio);
        if (ultimo == null || ultimo.getFechaSalida() == null) {
            throw new BusinessValidationException("El testimonio no fue retirado, no se puede reingresar");
        }

        MovimientoTestimonio nuevo = new MovimientoTestimonio();
        nuevo.setTestimonio(ultimo.getTestimonio());
        nuevo.setFechaIngreso(new Date());

        log.info("Reingresando testimonio id: {}", idTestimonio);
        return movimientoTestimonioRepository.save(nuevo);
    }
}
