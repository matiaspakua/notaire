package com.licensis.notaire.application.usecase.testimony;

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
 * Validates and applies the testimony movement circuit through the property registry:
 * ingreso, registration, retiro and reentry (CU11, CU12, CU44).
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
            throw new ResourceNotFoundException("Testimony not found with ID: " + idTestimony);
        }
    }

    /**
     * Presents a verified testimony for inscription, registering the entry date.
     *
     * @param idTestimony the testimony ID
     * @return the created movement
     * @throws ResourceNotFoundException if not testimony with the given ID exists
     * @throws BusinessValidationException if the testimony is not verified, or already has an open movement
     */
    public TestimonyMovement registerEntry(Integer idTestimony) {
        Testimony testimony = testimonyRepository.findById(idTestimony)
                .orElseThrow(() -> new ResourceNotFoundException("Testimony not found with ID: " + idTestimony));

        if (!testimony.getVerified()) {
            throw new BusinessValidationException("The testimony must be verified before registration entry");
        }

        TestimonyMovement latest = lastMovement(idTestimony);
        if (latest != null && latest.getDateExit() == null) {
            throw new BusinessValidationException("The testimony is already in registration");
        }

        TestimonyMovement movement = new TestimonyMovement();
        movement.setTestimony(testimony);
        movement.setDateEntry(new Date());

        log.info("Recording registration entry for testimony id: {}", idTestimony);
        return testimonyMovementRepository.save(movement);
    }

    /**
     * Marks the most recent movement of a testimony as registered, registering the date of registration.
     *
     * @param idTestimony the testimony id
     * @return the updated movement
     * @throws ResourceNotFoundException if no testimony with the given id exists
     * @throws BusinessValidationException if the testimony has no movement with a registered entry date
     */
    public TestimonyMovement registerInscription(Integer idTestimony) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement movement = Optional.ofNullable(lastMovement(idTestimony))
                .orElseThrow(() -> new BusinessValidationException(
                        "Record the registration entry before recording the inscription"));

        movement.setRegistered(true);
        movement.setDateRegistration(new Date());

        log.info("Recording registration of testimony id: {}", idTestimony);
        return testimonyMovementRepository.save(movement);
    }

    /**
     * Withdraws a registered testimony, recording the exit date and card number.
     *
     * @param idTestimony the testimony ID
     * @param cardNumber the card number
     * @return the updated movement
     * @throws ResourceNotFoundException if not testimony with the given ID exists
     * @throws BusinessValidationException if the testimony is not registered
     */
    public TestimonyMovement withdraw(Integer idTestimony, int cardNumber) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement movement = lastMovement(idTestimony);
        if (movement == null || !movement.getRegistered()) {
            throw new BusinessValidationException("The testimony is not registered");
        }

        movement.setDateExit(new Date());
        movement.setCardNumber(cardNumber);

        log.info("Recording withdrawal of testimony id: {}, card: {}", idTestimony, cardNumber);
        return testimonyMovementRepository.save(movement);
    }

    /**
     * Re-enters a withdrawn testimony, creating a new movement without altering the previous one.
     *
     * @param idTestimony the testimony ID
     * @param reentry card number, observation flag, and notes captured at re-entry
     * @return the newly created movement
     * @throws ResourceNotFoundException if not testimony with the given ID exists
     * @throws BusinessValidationException if the testimony was not previously withdrawn, or the reentry is
     *         observed without notes
     */
    public TestimonyMovement reenter(Integer idTestimony, TestimonyReentry reentry) {
        requireTestimonyExists(idTestimony);
        TestimonyMovement latest = lastMovement(idTestimony);
        if (latest == null || latest.getDateExit() == null) {
            throw new BusinessValidationException("The testimony was not withdrawn and cannot be re-entered");
        }
        if (reentry.isMissingRequiredNotes()) {
            throw new BusinessValidationException("An observed re-entry requires notes");
        }

        TestimonyMovement created = new TestimonyMovement();
        created.setTestimony(latest.getTestimony());
        created.setDateEntry(new Date());
        created.setCardNumber(reentry.cardNumber());
        created.setObservedByRegistry(reentry.observedByRegistry());
        created.setNotes(reentry.notes());

        log.info("Re-entering testimony id: {}", idTestimony);
        return testimonyMovementRepository.save(created);
    }
}
