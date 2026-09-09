package com.licensis.notaire.service;

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
 * CU82 - Generar Minuta de Inscripción: genera y hace seguimiento del
 * circuito registral (Generada -> Presentada -> Observada / Inscripta) de
 * una escritura sobre un inmueble ante el Registro de la Propiedad Inmueble.
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

    public RegistrationDraft generar(Integer idDeed) {
        Deed deed = deedRepository.findById(idDeed)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la escritura con ID: " + idDeed));

        if (!BusinessConstants.DeedFIRMADA.equals(deed.getStatus())) {
            throw new BusinessValidationException(
                    "La escritura debe estar firmada para generar la minuta de inscripción");
        }

        Property property = searchPropertyDelProcedure(idDeed);
        validarDatosCompletos(property);

        RegistrationDraft draft = new RegistrationDraft();
        draft.setNumber(calcularSiguienteNumber());
        draft.setStatus(BusinessConstants.RegistrationDraftGENERADA);
        draft.setDateGeneration(new Date());
        draft.setFkIdDeed(deed);
        return registrationDraftRepository.save(draft);
    }

    private Property searchPropertyDelProcedure(Integer idDeed) {
        return procedureRepository.findByFkIdDeedIdDeed(idDeed).stream()
                .map(Procedure::getFkIdProperty)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException(
                        "No existe un trámite con inmueble asociado a la escritura"));
    }

    private void validarDatosCompletos(Property property) {
        List<String> faltantes = new ArrayList<>();
        if (esVacio(property.getCadastralDesignation())) {
            faltantes.add("nomenclatura catastral");
        }
        if (property.getFiscalAppraisal() == null) {
            faltantes.add("valuación fiscal");
        }
        if (esVacio(property.getAddress())) {
            faltantes.add("domicilio");
        }
        if (esVacio(property.getRegistrationNumber())) {
            faltantes.add("matrícula");
        }
        if (esVacio(property.getVolumeFolioLandRecord())) {
            faltantes.add("tomo/folio/finca");
        }
        if (esVacio(property.getBoundaries())) {
            faltantes.add("linderos");
        }
        if (!faltantes.isEmpty()) {
            throw new BusinessValidationException(
                    "Faltan datos catastrales/registrales del inmueble: " + String.join(", ", faltantes));
        }
    }

    private boolean esVacio(String value) {
        return value == null || value.isBlank();
    }

    private int calcularSiguienteNumber() {
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
                    "La minuta debe estar en estado Generada para registrar la presentación");
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
                    "La minuta debe estar presentada para registrar una observación del Registro");
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
                    "La minuta debe estar presentada para registrar la inscripción definitiva");
        }
        draft.setDateReception(dateReception);
        draft.setFinalRegistrationNumber(finalRegistrationNumber);
        draft.setStatus(BusinessConstants.RegistrationDraftRegistered);
        return registrationDraftRepository.save(draft);
    }

    private RegistrationDraft searchDraft(Integer id) {
        return registrationDraftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la minuta de inscripción con ID: " + id));
    }
}
