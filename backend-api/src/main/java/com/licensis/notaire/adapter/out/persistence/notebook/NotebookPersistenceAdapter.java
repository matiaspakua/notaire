package com.licensis.notaire.adapter.out.persistence.notebook;

import com.licensis.notaire.application.port.out.notebook.NotebookRepositoryPort;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.NotebookRepository;
import com.licensis.notaire.repository.PersonRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for notebook slice.
 */
@Component
public class NotebookPersistenceAdapter implements NotebookRepositoryPort {

    private final NotebookRepository notebookRepository;
    private final PersonRepository personRepository;

    public NotebookPersistenceAdapter(NotebookRepository notebookRepository, PersonRepository personRepository) {
        this.notebookRepository = notebookRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Notebook save(Notebook entity) {
        return notebookRepository.save(entity);
    }

    @Override
    public Optional<Notebook> findById(Integer id) {
        return notebookRepository.findById(id);
    }

    @Override
    public List<Notebook> findAll() {
        return notebookRepository.findAll();
    }

    @Override
    public List<Notebook> findByYearAndNotary(Integer year, Person notary) {
        return notebookRepository.findByYearAndFkIdNotaryPerson(year, notary);
    }

    @Override
    public boolean existsByNumberYearAndNotary(Integer number, Integer year, Person notary) {
        return notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(number, year, notary);
    }

    @Override
    public Optional<Person> findPersonById(Integer id) {
        return personRepository.findById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return notebookRepository.existsById(id);
    }
}
