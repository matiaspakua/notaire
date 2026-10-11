package com.licensis.notaire.repository;

import com.licensis.notaire.business.PersonProcedure;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonProcedureRepository extends JpaRepository<PersonProcedure, Integer> {

    List<PersonProcedure> findByPerson(Person person);

    List<PersonProcedure> findByPersonIdPerson(Integer idPerson);

    List<PersonProcedure> findByProcedure(Procedure procedure);

    List<PersonProcedure> findByProcedureIdProcedure(Integer idProcedure);

    @Query("SELECT tp FROM PersonProcedure tp WHERE tp.person.idPerson = :idPerson AND tp.procedure.idProcedure = :idProcedure")
    List<PersonProcedure> findByPersonIdPersonAndProcedureIdProcedure(
            @Param("idPerson") Integer idPerson, @Param("idProcedure") Integer idProcedure);
}
