package com.licensis.notaire.repository;

import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    Optional<Budget> findByNumber(Integer number);

    List<Budget> findByFkIdPerson(Person person);

    List<Budget> findByFkIdPersonIdPerson(Integer idPerson);

    List<Budget> findByStatus(String status);

    @Query("SELECT p FROM Budget p WHERE p.fkIdPerson.idPerson = :idPersona AND p.status = :estado")
    List<Budget> findByFkIdPersonIdPersonAndStatus(@Param("idPersona") Integer idPerson, @Param("estado") String status);

    @Query("SELECT p FROM Budget p WHERE p.date BETWEEN :startDate AND :endDate")
    List<Budget> findByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
