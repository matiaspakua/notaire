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
                .orElseThrow(() -> new ResourceNotFoundException("There is no the deed with ID: " + idDeed));

        if (!BusinessConstants.DeedFIRMADA.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "The deed must be firmada for generate the registration draft");
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
        List<String> faltantes = new ArrayList<>();
        if (isEmptyValue(property.getCadastralDesignation())) {
            faltantes.add("cadastral nomenclature");
        }
        if (property.getFiscalAppraisal() == null) {
            faltantes.add("valuación fiscal");
        }
        if (isEmptyValue(property.getAddress())) {
            faltantes.add("domicilio");
        }
        if (isEmptyValue(property.getRegistrationNumber())) {
            faltantes.add("matrícula");
        }
        if (isEmptyValue(property.getVolumeFolioLandRecord())) {
            faltantes.add("tomo/folios/finca");
        }
        if (isEmptyValue(property.getBoundaries())) {
            faltantes.add("linderos");
        }
        if (!faltantes.isEmpty()) {
            throw new BusinessValidationException(
                    "Faltan datos catastrales/registrales of the property: " + String.join(", ", faltantes));
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

    public RegistrationDraft presentar(Integer id, Date dateSubmission, String registryEntryNumber) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftGENERADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The minuta must be in status Generada for record the presentación");
        }
        draft.setDateSubmission(dateSubmission);
        draft.setRegistryEntryNumber(registryEntryNumber);
        draft.setStatus(BusinessConstants.RegistrationDraftPRESENTADA);
        return registrationDraftRepository.save(draft);
    }

    public RegistrationDraft observar(Integer id, String registryNotes, Date dateCorrection) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftPRESENTADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The minuta must be presented for record a observation of the Registro");
        }
        draft.setRegistryNotes(registryNotes);
        draft.setDateCorrection(dateCorrection);
        draft.setStatus(BusinessConstants.RegistrationDraftOBSERVADA);
        return registrationDraftRepository.save(draft);
    }

    public RegistrationDraft inscribir(Integer id, Date dateReception, String finalRegistrationNumber) {
        RegistrationDraft draft = searchDraft(id);
        if (!BusinessConstants.RegistrationDraftPRESENTADA.equals(draft.getStatus())) {
            throw new BusinessValidationException(
                    "The minuta must be presented for record the registration definitiva");
        }
        draft.setDateReception(dateReception);
        draft.setFinalRegistrationNumber(finalRegistrationNumber);
        draft.setStatus(BusinessConstants.RegistrationDraftRegistered);
        return registrationDraftRepository.save(draft);
    }

    private RegistrationDraft searchDraft(Integer id) {
        return registrationDraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no the registration draft with ID: " + id));
    }
}
