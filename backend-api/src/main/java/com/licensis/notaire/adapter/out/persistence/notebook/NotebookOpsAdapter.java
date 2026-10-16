package com.licensis.notaire.adapter.out.persistence.notebook;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.out.notebook.NotebookOpsPort;
import com.licensis.notaire.application.usecase.notebook.NotebookService;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

/**
 * Persistence/ops adapter for notebooks. Holds entity access previously on
 * {@code NotebookController} (issue #1817).
 */
@Component
public class NotebookOpsAdapter implements NotebookOpsPort {

    private final NotebookService notebookService;

    public NotebookOpsAdapter(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotebookView> findAll() {
        return notebookService.findAll().stream().map(this::toView).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NotebookView> findById(Integer id) {
        return notebookService.findById(id).map(this::toView);
    }

    @Override
    @Transactional
    public CreateResult create(List<Integer> idsFolio, Integer idNotary, int year, String notes) {
        try {
            Notebook created = notebookService.createNotebook(idsFolio, idNotary, year, notes);
            return new CreateResult.Created(toView(created));
        } catch (BusinessValidationException e) {
            return new CreateResult.BadRequest(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return new CreateResult.NotFound(e.getMessage());
        }
    }

    private NotebookView toView(Notebook notebook) {
        return new NotebookView(
                notebook.getIdNotebook(),
                notebook.getNumber(),
                notebook.getYear(),
                notebook.getNotes(),
                toNotaryView(notebook.getFkIdNotaryPerson()),
                notebook.getVersion());
    }

    private static NotaryView toNotaryView(Person notary) {
        if (notary == null) {
            return null;
        }
        return new NotaryView(
                notary.getPersonId(),
                notary.getFirstName(),
                notary.getLastName(),
                notary.getIdentificationNumber(),
                notary.getNotaryRegistrationNumber());
    }
}
