package com.licensis.notaire.repository;

import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.DeedManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedure, Integer> {

    List<Procedure> findByFkIdProcedureType(ProcedureType typeProcedure);

    List<Procedure> findByFkIdProcedureTypeIdProcedureType(Integer idProcedureType);

    List<Procedure> findByFkIdManagement(DeedManagement management);

    List<Procedure> findByFkIdManagementIdManagement(Integer idManagement);

    List<Procedure> findByFkIdBudgetIdBudget(Integer idBudget);

    List<Procedure> findByFkIdDeedIdDeed(Integer idDeed);

    @Query("SELECT t FROM Procedure t JOIN t.personList p WHERE p.idPerson = :idPersona")
    List<Procedure> findByPersonListIdPerson(@Param("idPersona") Integer idPerson);
}
