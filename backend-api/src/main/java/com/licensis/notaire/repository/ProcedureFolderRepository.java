package com.licensis.notaire.repository;

import com.licensis.notaire.business.ProcedureFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcedureFolderRepository extends JpaRepository<ProcedureFolder, Integer> {

    Optional<ProcedureFolder> findByFkIdProcedureIdProcedure(Integer idProcedure);

    List<ProcedureFolder> findByFkIdManagementIdManagement(Integer idManagement);

    List<ProcedureFolder> findByFkIdManagementIdManagementAndStatus(Integer idManagement, String status);

    /** Next folder number from a database sequence, so concurrent callers never share one. */
    @Query(value = "SELECT CAST(nextval('procedure_folder_number_seq') AS integer)", nativeQuery = true)
    int nextFolderNumber();
}
