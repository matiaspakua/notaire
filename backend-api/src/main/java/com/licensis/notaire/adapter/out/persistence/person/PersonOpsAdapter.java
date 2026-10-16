package com.licensis.notaire.adapter.out.persistence.person;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.person.PersonOpsPort;
import com.licensis.notaire.application.usecase.person.PersonService;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.DuplicatePersonException;
import com.licensis.notaire.repository.IdentificationTypeRepository;

/**
 * Persistence/ops adapter for people. Holds entity/IdentificationTypeRepository access previously on
 * {@code PersonController} (issue #1813).
 */
@Component
public class PersonOpsAdapter implements PersonOpsPort {

    static final String DEFAULT_IDENTIFICATION_TYPE = "DNI";

    private final PersonService personService;
    private final IdentificationTypeRepository identificationTypeRepository;

    public PersonOpsAdapter(PersonService personService, IdentificationTypeRepository identificationTypeRepository) {
        this.personService = personService;
        this.identificationTypeRepository = identificationTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        var pageable = PageRequest.of(page, size, parseSort(sort));
        var result = personService.findAll(pageable).map(this::toView);
        return new PageResult(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonView> findById(Integer id) {
        return personService.findById(id).map(this::toView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonView> search(
            String firstName,
            String lastName,
            String identificationNumber,
            Integer idIdentificationType,
            Boolean isClient) {
        return personService.search(firstName, lastName, identificationNumber, idIdentificationType, isClient)
                .stream()
                .map(this::toView)
                .toList();
    }

    @Override
    // No outer @Transactional: catching into SaveResult must not mark TX rollback-only
    // (UnexpectedRollbackException → 500 instead of 4xx).
    public SaveResult create(PersonCommand command) {
        IdentificationType type = resolveType(command, this::defaultIdentificationType);
        try {
            Person person = new Person();
            applyCommand(person, command, type);
            return new SaveResult.Created(toView(personService.save(person)));
        } catch (DuplicatePersonException e) {
            return new SaveResult.Duplicate(e.getMessage(), e.getExistingPersonId());
        } catch (Exception e) {
            return new SaveResult.Failed(e, true);
        }
    }

    @Override
    public SaveResult update(Integer id, PersonCommand command) {
        return personService.findById(id).<SaveResult>map(existing -> {
            IdentificationType type = resolveType(command, existing::getFkIdIdentificationType);
            applyCommand(existing, command, type);
            try {
                return new SaveResult.Updated(toView(personService.save(existing)));
            } catch (DuplicatePersonException e) {
                return new SaveResult.Duplicate(e.getMessage(), e.getExistingPersonId());
            } catch (Exception e) {
                return new SaveResult.Failed(e, false);
            }
        }).orElseGet(SaveResult.NotFound::new);
    }

    @Override
    public DeleteResult delete(Integer id) {
        if (personService.findById(id).isEmpty()) {
            return new DeleteResult.NotFound();
        }
        try {
            personService.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            return new DeleteResult.Conflict("Cannot delete: person is referenced by other records.");
        }
    }

    private static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "idPerson");
        }
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private Integer resolveIdentificationTypeId(PersonCommand command) {
        if (command.identificationTypeId() != null) {
            return command.identificationTypeId();
        }
        return command.nestedIdentificationTypeId();
    }

    /**
     * The requested identification type, {@code fallback} when none was sent, or a 400 when the id
     * is not in the catalog (it used to fall back silently, so a typo stored DNI; #1361).
     */
    private IdentificationType resolveType(PersonCommand command, Supplier<IdentificationType> fallback) {
        Integer typeId = resolveIdentificationTypeId(command);
        if (typeId == null) {
            return fallback.get();
        }
        return identificationTypeRepository.findById(typeId)
                .orElseThrow(() -> new BusinessValidationException(
                        "identificationTypeId: unknown identification type " + typeId));
    }

    /** DNI, the default type of a new person (Owner decision on #1361), found by name, not by id. */
    private IdentificationType defaultIdentificationType() {
        return identificationTypeRepository.findByName(DEFAULT_IDENTIFICATION_TYPE)
                .orElseGet(() -> {
                    IdentificationType ti = new IdentificationType();
                    ti.setName(DEFAULT_IDENTIFICATION_TYPE);
                    return identificationTypeRepository.save(ti);
                });
    }

    private void applyCommand(Person person, PersonCommand command, IdentificationType type) {
        person.setFirstName(command.firstName());
        person.setLastName(command.lastName());
        person.setIdentificationNumber(command.identificationNumber());
        person.setIsClient(command.isClient());
        person.setNationality(command.nationality());
        person.setTaxId(command.taxId());
        person.setSex(command.sex());
        person.setBirthDate(command.birthDate());
        person.setMaritalStatus(command.maritalStatus());
        person.setMarriageCount(command.marriageCount());
        person.setOccupation(command.occupation());
        person.setAddress(command.address());
        person.setPhone(command.phone());
        person.setEmail(command.email());
        person.setNotaryRegistrationNumber(command.notaryRegistrationNumber());
        if (type != null) {
            person.setFkIdIdentificationType(type);
        }
    }

    private PersonView toView(Person person) {
        Integer typeId = person.getFkIdIdentificationType() != null
                ? person.getFkIdIdentificationType().getIdIdentificationType()
                : null;
        return new PersonView(
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
