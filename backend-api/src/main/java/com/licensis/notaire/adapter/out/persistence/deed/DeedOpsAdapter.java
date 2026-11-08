package com.licensis.notaire.adapter.out.persistence.deed;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.deed.DeedOpsPort;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.FolioRepository;

/**
 * Persistence/ops adapter for deeds. Holds entity/FolioRepository access previously on
 * {@code DeedController} (issue #1811).
 */
@Component
public class DeedOpsAdapter implements DeedOpsPort {

    /** Deed statuses as stored (BusinessConstants), in workflow order. */
    static final List<String> DEED_STATUSES = List.of(
            BusinessConstants.DeedUnsigned, BusinessConstants.DeedSigned, BusinessConstants.DeedVoided,
            BusinessConstants.DeedNotPassed, BusinessConstants.DeedRegistered);

    private final DeedService deedService;
    private final DeedSigningService deedSigningService;
    private final FolioRepository folioRepository;

    public DeedOpsAdapter(
            DeedService deedService, DeedSigningService deedSigningService, FolioRepository folioRepository) {
        this.deedService = deedService;
        this.deedSigningService = deedSigningService;
        this.folioRepository = folioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(String status, int page, int size, String sort) {
        var pageable = PageRequest.of(page, size, parseSort(sort));
        var result = deedService.findAllPaged(canonicalStatus(status), pageable).map(this::toView);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeedView> findById(Integer id) {
        return deedService.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeedView> searchByNumber(Integer number) {
        return deedService.searchByNumber(number).stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaryView> findAvailableNotaries() {
        return deedService.findAvailableNotaries().stream().map(this::toNotaryView).toList();
    }

    @Override
    @Transactional
    public SaveResult create(DeedCommand command) {
        Deed entity = new Deed();
        applyCommand(entity, command);
        Deed saved = deedService.save(entity);
        linkFolio(saved, command.idFolio());
        return new SaveResult.Created(toView(saved));
    }

    @Override
    @Transactional
    public SaveResult update(Integer id, DeedCommand command) {
        return deedService.findById(id).<SaveResult>map(existing -> {
            applyCommand(existing, command);
            return new SaveResult.Updated(toView(deedService.save(existing)));
        }).orElseGet(SaveResult.NotFound::new);
    }

    @Override
    @Transactional
    public DeleteResult delete(Integer id) {
        if (deedService.findById(id).isEmpty()) {
            return new DeleteResult.NotFound();
        }
        deedService.deleteById(id);
        return new DeleteResult.Deleted();
    }

    @Override
    public DeedView sign(Integer id) {
        return toView(deedSigningService.sign(id));
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    /** The stored spelling of {@code value} in any letter case, null for none, or a 400. */
    static String canonicalStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (String known : DEED_STATUSES) {
            if (known.equalsIgnoreCase(value.trim())) {
                return known;
            }
        }
        throw new BusinessValidationException("status: invalid value '" + value + "'; accepted values: "
                + String.join(", ", DEED_STATUSES));
    }

    private void applyCommand(Deed deed, DeedCommand command) {
        if (command.number() != null) {
            deed.setNumber(command.number());
        }
        deed.setBody(command.body());
        String status = command.status();
        if (status == null || status.isBlank()) {
            status = "Sin Firmar";
        }
        deed.setStatus(status);
        deed.setDateDeedrecording(command.dateDeedrecording());
        deed.setDateRegistration(command.dateRegistration());
        deed.setRegistrationEntryNumber(command.registrationEntryNumber());
        deed.setNotes(command.notes());
    }

    private void linkFolio(Deed deed, Integer idFolio) {
        if (idFolio == null) {
            return;
        }
        folioRepository.findById(idFolio).ifPresent(folio -> {
            folio.setFkIdDeed(deed);
            folioRepository.save(folio);
        });
    }

    private DeedView toView(Deed deed) {
        return new DeedView(
                deed.getIdDeed(),
                deed.getNumber(),
                deed.getBody(),
                deed.getStatus(),
                deed.getDateDeedrecording(),
                deed.getDateRegistration(),
                deed.getRegistrationEntryNumber(),
                deed.getNotes(),
                deed.getVersion());
    }

    private NotaryView toNotaryView(Person person) {
        Integer typeId = person.getFkIdIdentificationType() != null
                ? person.getFkIdIdentificationType().getIdIdentificationType()
                : null;
        return new NotaryView(
                person.getPersonId(),
                person.getFirstName(),
                person.getLastName(),
                person.getIdentificationNumber(),
                person.getIsClient(),
                person.getNationality(),
                person.getTaxId(),
                person.getSex(),
                person.getBirthDate(),
                person.getMaritalStatus(),
                person.getMarriageCount(),
                person.getOccupation(),
                person.getAddress(),
                person.getPhone(),
                person.getEmail(),
                person.getNotaryRegistrationNumber(),
                typeId,
                person.getVersion());
    }
}
