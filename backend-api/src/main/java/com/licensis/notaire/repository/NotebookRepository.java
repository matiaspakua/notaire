package com.licensis.notaire.repository;

import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, Integer> {

    List<Notebook> findByYearAndFkIdNotaryPerson(int year, Person notary);

    boolean existsByNumberAndYearAndFkIdNotaryPerson(int number, int year, Person notary);
}
