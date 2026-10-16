package com.licensis.notaire.adapter.out.persistence.substitution;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Persistence/ops adapter for substitutions. Holds entity/PersonRepository access previously on
 * {@code SubstitutionController} (issue #1807).
 */
@Component
public class SubstitutionOpsAdapter implements SubstitutionOpsPort {

    private static final Logger log = LoggerFactory.getLogger(SubstitutionOpsAdapter.class);

    private final SubstitutionService service;
    private final PersonRepository personRepository;

    public SubstitutionOpsAdapter(SubstitutionService service, PersonRepository personRepository) {
        this.service = service;
        this.personRepository = personRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubstitutionView> findAll() {
        return service.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubstitutionView> findById(Integer id) {
        return service.findById(id).map(this::toView);
    }

    @Override
    public SaveResult create(SubstitutionCommand command) {
        try {
            Substitution entity = new Substitution();
            if (!applyCommand(entity, command)) {
                return new SaveResult.BadRequest();
            }
            return new SaveResult.Created(toView(service.save(entity)));
        } catch (Exception e) {
            log.error("Failed to create substitution", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public SaveResult update(Integer id, SubstitutionCommand command) {
        Substitution existing = service.findById(id).orElse(null);
        if (existing == null) {
            return new SaveResult.NotFound();
        }
        try {
            if (!applyCommand(existing, command)) {
                return new SaveResult.BadRequest();
            }
            service.save(existing);
            return new SaveResult.Updated();
        } catch (Exception e) {
            log.error("Failed to update substitution id {}", id, e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public DeleteResult delete(Integer id) {
        if (!service.existsById(id)) {
            return new DeleteResult.NotFound();
        }
        try {
            service.deleteById(id);
            return new DeleteResult.Deleted();
        } catch (Exception e) {
            log.error("Failed to delete substitution id {}", id, e);
            return new DeleteResult.ServerError();
        }
    }

    private boolean applyCommand(Substitution entity, SubstitutionCommand command) {
        Integer substituteId = command.substitutePersonId();
        Integer substitutedId = command.substitutedPersonId();
        if (substituteId == null || substitutedId == null) {
            return false;
        }
        Person substitute = personRepository.findById(substituteId).orElse(null);
        Person substituted = personRepository.findById(substitutedId).orElse(null);
        if (substitute == null || substituted == null) {
            return false;
        }
        entity.setDateStart(command.dateStart());
        entity.setDateEnd(command.dateEnd());
        entity.setNotes(command.notes());
        entity.setFkIdSubstitute(substitute);
        entity.setFkIdSubstituted(substituted);
        return true;
    }

    private PersonView toPersonView(Person person) {
        if (person == null || person.getPersonId() == null) {
            return null;
        }
        return new PersonView(person.getPersonId(), person.getFirstName(), person.getLastName());
    }

    private SubstitutionView toView(Substitution entity) {
        Person substitute = entity.getFkIdSubstitute();
        Person substituted = entity.getFkIdSubstituted();
        Integer substituteId = substitute != null ? substitute.getPersonId() : null;
        Integer substitutedId = substituted != null ? substituted.getPersonId() : null;
        return new SubstitutionView(
                entity.getIdSubstitution(),
                entity.getDateStart(),
                entity.getDateEnd(),
                entity.getNotes(),
                substituteId,
                substitutedId,
                toPersonView(substitute),
                toPersonView(substituted),
                entity.getVersion());
    }
}
