package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.dto.DtoCaseDeed;
import com.licensis.notaire.dto.DtoCaseDocument;
import com.licensis.notaire.dto.DtoCaseTestimony;
import com.licensis.notaire.dto.DtoManagementCaseSummary;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Summary of what a gestión has produced after signing: the distinct escrituras of its trámites, their
 * testimonios with the state of the registry circuit (CU11, CU12, CU44) and the copias issued (CU70).
 */
@Service
public class ManagementCaseSummaryService {

    static final String NOT_ENTERED = "SIN_INGRESAR";
    static final String ENTERED = "INGRESADO";
    static final String REGISTERED = "INSCRIPTO";
    static final String WITHDRAWN = "RETIRADO";

    private final DeedManagementRepository managementRepository;
    private final ProcedureRepository procedureRepository;
    private final SubmittedDocumentRepository submittedDocumentRepository;

    public ManagementCaseSummaryService(DeedManagementRepository managementRepository,
            ProcedureRepository procedureRepository, SubmittedDocumentRepository submittedDocumentRepository) {
        this.managementRepository = managementRepository;
        this.procedureRepository = procedureRepository;
        this.submittedDocumentRepository = submittedDocumentRepository;
    }

    /**
     * @param idManagement the gestión ID
     * @return the case summary, with an empty deed list when no trámite has an escritura
     * @throws ResourceNotFoundException if the gestión does not exist
     */
    @Transactional(readOnly = true)
    public DtoManagementCaseSummary getSummary(Integer idManagement) {
        DeedManagement management = managementRepository.findById(idManagement)
                .orElseThrow(() -> new ResourceNotFoundException("Gestión no encontrada con ID: " + idManagement));

        Map<Integer, Deed> deedsById = procedureRepository.findByFkIdManagementIdManagement(idManagement).stream()
                .map(Procedure::getFkIdDeed)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Deed::getIdDeed, deed -> deed, (first, duplicate) -> first,
                        java.util.LinkedHashMap::new));

        List<DtoCaseDeed> deeds = deedsById.values().stream().map(ManagementCaseSummaryService::toCaseDeed).toList();
        List<DtoCaseDocument> documents = submittedDocumentRepository
                .findByFkIdProcedureFkIdManagementIdManagement(idManagement).stream()
                .map(ManagementCaseSummaryService::toCaseDocument)
                .toList();
        return new DtoManagementCaseSummary(management.getIdManagement(), management.getNumber(),
                management.getEncabezado(), deeds, documents);
    }

    private static DtoCaseDocument toCaseDocument(SubmittedDocument document) {
        DocumentType type = document.getDocumentType();
        return new DtoCaseDocument(document.getIdSubmittedDocument(), document.getName(),
                type == null ? null : type.getName(), document.getFkIdProcedure().getIdProcedure(),
                document.getPrepared(), Boolean.TRUE.equals(document.getReleased()),
                Boolean.TRUE.equals(document.getFlagged()), Boolean.TRUE.equals(document.getDelivered()),
                Boolean.TRUE.equals(document.getReentered()), toLocalDate(document.getDateDue()));
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : new Date(date.getTime()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static DtoCaseDeed toCaseDeed(Deed deed) {
        List<DtoCaseTestimony> testimonies = nullToEmpty(deed.getTestimonyList()).stream()
                .map(ManagementCaseSummaryService::toCaseTestimony)
                .toList();
        return new DtoCaseDeed(deed.getIdDeed(), deed.getNumber(), deed.getStatus(), testimonies);
    }

    private static DtoCaseTestimony toCaseTestimony(Testimony testimony) {
        return new DtoCaseTestimony(testimony.getIdTestimony(), testimony.getNumber(), testimony.getVerified(),
                testimony.getFlagged(), stateOf(testimony), nullToEmpty(testimony.getCopyList()).size());
    }

    private static String stateOf(Testimony testimony) {
        return nullToEmpty(testimony.getTestimonyMovementList()).stream()
                .max(Comparator.comparing(TestimonyMovement::getIdTestimonyMovement,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(ManagementCaseSummaryService::stateOf)
                .orElse(NOT_ENTERED);
    }

    private static String stateOf(TestimonyMovement latest) {
        if (latest.getDateExit() != null) {
            return WITHDRAWN;
        }
        return latest.getRegistered() ? REGISTERED : ENTERED;
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> values) {
        return values == null ? List.of() : values;
    }
}
