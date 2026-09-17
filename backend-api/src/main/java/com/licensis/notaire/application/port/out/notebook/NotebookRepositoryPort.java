package com.licensis.notaire.application.port.out.notebook;

import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Person;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for notebook persistence operations.
 */
public interface NotebookRepositoryPort {

    Notebook save(Notebook entity);

    Optional<Notebook> findById(Integer id);

    List<Notebook> findAll();

    List<Notebook> findByYearAndNotary(Integer year, Person notary);

    boolean existsByNumberYearAndNotary(Integer number, Integer year, Person notary);

    Optional<Person> findPersonById(Integer id);

    boolean existsById(Integer id);
}
