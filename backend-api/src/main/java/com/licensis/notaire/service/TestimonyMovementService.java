package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import com.licensis.notaire.repository.TestimonyRepository;
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
public class TestimonyMovementService {

    private static final Logger log = LoggerFactory.getLogger(TestimonyMovementService.class);

    private final TestimonyMovementRepository testimonyMovementRepository;
    private final TestimonyRepository testimonyRepository;

    public TestimonyMovementService(TestimonyMovementRepository testimonyMovementRepository,
            TestimonyRepository testimonyRepository) {
        this.testimonyMovementRepository = testimonyMovementRepository;
        this.testimonyRepository = testimonyRepository;
    }

    private TestimonyMovement lastMovement(Integer idTestimony) {
        return testimonyMovementRepository
                .findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(idTestimony)
                .orElse(null);
    }

    private void requireTestimonyExists(Integer idTestimony) {
        if (!testimonyRepository.existsById(idTestimony)) {
            throw new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimony);
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
    public TestimonyMovement registerEntry(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimonio no encontrado con ID: " + idTestimony));

        if (!testimony.getVerified()) {
            throw new BusinessValidationException("El testimonio debe estar verificado para ingresar a inscripción");
        }

        TestimonyMovement ultimo = lastMovement(idTestimony);
        if (ultimo != null && ultimo.getDateExit() == null) {
            throw new BusinessValidationException("El testimonio ya está en trámite de inscripción");
        }

        TestimonyMovement movement = new TestimonyMovement();
        movement.setTestimony(testimony);
        movement.setDateEntry(new Date());

        log.info("Registrando ingreso a inscripción del testimonio id: {}", idTestimony);
        return testimonyMovementRepository.save(movement);
    }

    /**
     * Marks the most recent movement of a testimonio as inscripto, registering the fecha de inscripción.
     *
     * @param idTestimonio the testimonio ID
     * @return the updated movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio has no movement with fecha de ingreso registered
     */
    public TestimonyMovement registerInscription(Integer idTestimony) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement movement = Optional.ofNullable(lastMovement(idTestimony))
                .orElseThrow(() -> new BusinessValidationException(
                        "Falta el ingreso a inscripción del testimonio antes de registrar la inscripción"));

        movement.setRegistered(true);
        movement.setDateRegistration(new Date());

        log.info("Registrando inscripción del testimonio id: {}", idTestimony);
        return testimonyMovementRepository.save(movement);
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
    public TestimonyMovement withdraw(Integer idTestimony, int cardNumber) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement movement = lastMovement(idTestimony);
        if (movement == null || !movement.getRegistered()) {
            throw new BusinessValidationException("El testimonio no está inscripto");
        }

        movement.setDateExit(new Date());
        movement.setCardNumber(cardNumber);

        log.info("Registrando retiro del testimonio id: {}, cartón: {}", idTestimony, cardNumber);
        return testimonyMovementRepository.save(movement);
    }

    /**
     * Re-enters a withdrawn testimonio, creating a new movement without altering the previous one.
     *
     * @param idTestimonio the testimonio ID
     * @return the newly created movement
     * @throws ResourceNotFoundException if no testimonio with the given ID exists
     * @throws BusinessValidationException if the testimonio was not previously withdrawn
     */
    public TestimonyMovement reenter(Integer idTestimony) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement ultimo = lastMovement(idTestimony);
        if (ultimo == null || ultimo.getDateExit() == null) {
            throw new BusinessValidationException("El testimonio no fue retirado, no se puede reenter");
        }

        TestimonyMovement nuevo = new TestimonyMovement();
        nuevo.setTestimony(ultimo.getTestimony());
        nuevo.setDateEntry(new Date());

        log.info("Reingresando testimonio id: {}", idTestimony);
        return testimonyMovementRepository.save(nuevo);
    }
}
