package com.licensis.notaire.adapter.out.persistence.copy;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.copy.CopyOpsPort;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.TestimonyRepository;

/**
 * Persistence/ops adapter for copies. Holds repository access previously on
 * {@code CopyController} (issue #1779).
 */
@Component
public class CopyOpsAdapter implements CopyOpsPort {

    private static final Logger log = LoggerFactory.getLogger(CopyOpsAdapter.class);

    private final CopyService service;
    private final PersonRepository personRepository;
    private final TestimonyRepository testimonyRepository;

    public CopyOpsAdapter(
            CopyService service,
            PersonRepository personRepository,
            TestimonyRepository testimonyRepository) {
        this.service = service;
        this.personRepository = personRepository;
        this.testimonyRepository = testimonyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CopyView> findAll() {
        return service.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CopyView> findById(Integer id) {
        return service.findById(id).map(this::toView);
    }

    @Override
    public SaveResult create(CopyCommand command) {
        if (command.testimonyId() != null && !service.canCreateCopyForTestimony(command.testimonyId())) {
            return new SaveResult.Conflict(
                    "The testimony already has a registered movement and does not accept new copies.");
        }
        try {
            Copy entity = new Copy();
            applyRequest(entity, command);
            entity = service.save(entity);
            return new SaveResult.Created(toView(entity));
        } catch (Exception e) {
            log.error("Failed to create copy", e);
            return new SaveResult.ServerError();
        }
    }

    @Override
    public SaveResult update(Integer id, CopyCommand command) {
        Copy existing = service.findById(id).orElse(null);
        if (existing == null) {
            return new SaveResult.NotFound();
        }
        try {
            applyRequest(existing, command);
            service.save(existing);
            return new SaveResult.Updated();
        } catch (Exception e) {
            log.error("Failed to update copy id {}", id, e);
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
            log.error("Failed to delete copy id {}", id, e);
            return new DeleteResult.Conflict();
        }
    }

    private CopyView toView(Copy copy) {
        Integer personId = copy.getFkIdPerson() != null ? copy.getFkIdPerson().getPersonId() : null;
        Integer testimonyId = copy.getFkIdTestimony() != null ? copy.getFkIdTestimony().getIdTestimony() : null;
        return new CopyView(
                copy.getIdCopy(),
                copy.getNumber(),
                copy.getDatePrinting(),
                copy.getDateWithdrawal(),
                copy.getNotes(),
                personId,
                testimonyId,
                copy.getVersion());
    }

    private void applyRequest(Copy copy, CopyCommand command) {
        if (command.number() != null) {
            copy.setNumber(command.number());
        }
        copy.setDatePrinting(command.datePrinting());
        copy.setDateWithdrawal(command.dateWithdrawal());
        copy.setNotes(command.notes());
        if (command.personId() != null) {
            Person person = personRepository.findById(command.personId()).orElse(null);
            copy.setFkIdPerson(person);
        } else {
            copy.setFkIdPerson(null);
        }
        if (command.testimonyId() != null) {
            Testimony testimony = testimonyRepository.findById(command.testimonyId()).orElse(null);
            copy.setFkIdTestimony(testimony);
        } else {
            copy.setFkIdTestimony(null);
        }
    }
}
