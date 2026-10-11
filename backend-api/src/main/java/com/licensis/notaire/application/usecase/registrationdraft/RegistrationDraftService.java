package com.licensis.notaire.application.usecase.registrationdraft;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.RegistrationDraft;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.RegistrationDraftRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * CU82 - Generate Registration draft: generates and hace seguimiento of the
 * circuito registral (Generada -> Presentada -> Observada / Inscripta) de
 * a deed on a property ante the Registro of the Propiedad Property.
 */
@Service
@Transactional
public class RegistrationDraftService {

    private final RegistrationDraftRepository registrationDraftRepository;
    private final DeedRepository deedRepository;
    private final ProcedureRepository procedureRepository;

    public RegistrationDraftService(RegistrationDraftRepository registrationDraftRepository,
            DeedRepository deedRepository, ProcedureRepository procedureRepository) {
        this.registrationDraftRepository = registrationDraftRepository;
        this.deedRepository = deedRepository;
        this.procedureRepository = procedureRepository;
    }

    public RegistrationDraft generate(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + idDeed));

        if (!BusinessConstants.DeedFIRMADA.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "The deed must be in status 'Firmada' to generate the registration draft");
        }

        Property property = findProcedureProperty(idDeed);
        validateCompleteData(property);

        RegistrationDraft draft = new RegistrationDraft();
        draft.setNumber(calculateNextNumber());
        draft.setStatus(BusinessConstants.RegistrationDraftGENERADA);
        draft.setDateGeneration(new Date());
        draft.setFkIdDeed(deed);
        return registrationDraftRepository.save(draft);
    }

    private Property findProcedureProperty(Integer idDeed) {
        return procedureRepository.findByFkIdDeedIdDeed(idDeed).stream()
                .map(Procedure::getFkIdProperty)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException(
                        "There is no procedure with property associated to the deed"));
    }

    private void validateCompleteData(Property property) {
        List<String> missing = new ArrayList<>();
        if (isEmptyValue(property.getCadastralDesignation())) {
            missing.add("cadastral nomenclature");
        }
        if (property.getFiscalAppraisal() == null) {
            missing.add("fiscal appraisal");
        }
        if (isEmptyValue(property.getAddress())) {
            missing.add("address");
        }
        if (isEmptyValue(property.getRegistrationNumber())) {
            missing.add("registration number");
        }
        if (isEmptyValue(property.getVolumeFolioLandRecord())) {
            missing.add("volume/folios/parcel");
        }
        if (isEmptyValue(property.getBoundaries())) {
            missing.add("boundaries");
        }
        if (!missing.isEmpty()) {
            throw new BusinessValidationException(
                    "Missing cadastral/registry data for the property: " + String.join(", ", missing));
        }
    }

    private boolean isEmptyValue(String value) {
        return value == null || value.isBlank();
    }

    private int calculateNextNumber() {
        return registrationDraftRepository.findTopByOrderByNumberDesc()
                .map(m -> m.getNumber() + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Optional<RegistrationDraft> findById(Integer id) {
        return registrationDraftRepository.findById(id);
    }

    public RegistrationDraft submit(Integer id, Date dateSubmission, String registryEntryNumber) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftGENERADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The registration draft must be in status Generada to record the submission");
        }
        draft.setDateSubmission(dateSubmission);
        draft.setRegistryEntryNumber(registryEntryNumber);
        draft.setStatus(BusinessConstants.RegistrationDraftPRESENTADA);
        return registrationDraftRepository.save(draft);
    }

    public RegistrationDraft observe(Integer id, String registryNotes, Date dateCorrection) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftPRESENTADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The registration draft must be submitted to record a registry observation");
        }
        draft.setRegistryNotes(registryNotes);
        draft.setDateCorrection(dateCorrection);
        draft.setStatus(BusinessConstants.RegistrationDraftOBSERVADA);
        return registrationDraftRepository.save(draft);
    }

    public RegistrationDraft recordRegistration(Integer id, Date dateReception, String finalRegistrationNumber) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftPRESENTADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The registration draft must be submitted to record the final registration");
        }
        draft.setDateReception(dateReception);
        draft.setFinalRegistrationNumber(finalRegistrationNumber);
        draft.setStatus(BusinessConstants.RegistrationDraftRegistered);
        return registrationDraftRepository.save(draft);
    }

    private RegistrationDraft searchDraft(Integer id) {
        return registrationDraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration draft not found with ID: " + id));
    }
}
