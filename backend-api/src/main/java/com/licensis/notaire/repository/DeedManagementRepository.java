package com.licensis.notaire.repository;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.ManagementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeedManagementRepository extends JpaRepository<DeedManagement, Integer> {

    List<DeedManagement> findByFkIdNotaryPerson(Person notary);

    List<DeedManagement> findByFkIdNotaryPersonIdPerson(Integer idNotary);

    List<DeedManagement> findByFkIdManagementStatus(ManagementStatus status);

    List<DeedManagement> findByFkIdManagementStatusIdManagementStatus(Integer idStatus);

    @Query("SELECT g FROM DeedManagement g WHERE g.fkIdNotaryPerson.idPerson = :idEscribano "
            + "AND g.fkIdManagementStatus.idManagementStatus = :idEstado")
    List<DeedManagement> findByFkIdNotaryPersonIdPersonAndFkIdStatusIdManagementStatus(
            @Param("idEscribano") Integer idNotary, @Param("idEstado") Integer idStatus);

    @Query("SELECT g FROM DeedManagement g WHERE g.dateStart BETWEEN :startDate AND :endDate")
    List<DeedManagement> findByDateStartBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT g FROM DeedManagement g WHERE g.notes LIKE %:keyword%")
    List<DeedManagement> findByNotesContaining(@Param("keyword") String keyword);

    java.util.Optional<DeedManagement> findByNumber(int number);

    @Query("SELECT DISTINCT g FROM DeedManagement g JOIN g.procedureList t "
            + "WHERE t.fkIdBudget.fkIdPerson.idPerson = :idPersona")
    List<DeedManagement> findByClientPersonId(@Param("idPersona") Integer idPerson);
}
