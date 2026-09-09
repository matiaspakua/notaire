package com.licensis.notaire.repository;

import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.FolioType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, Integer> {

    Optional<Folio> findByNumber(int number);

    List<Folio> findByFkIdFolioType(FolioType typeFolio);

    List<Folio> findByFkIdFolioTypeIdFolioType(Integer idFolioType);

    List<Folio> findByFkIdNotaryPerson(Person notary);

    List<Folio> findByFkIdNotaryPersonIdPerson(Integer idNotary);

    boolean existsByFkIdDeedIdDeed(Integer idDeed);

    Optional<Folio> findByFkIdDeedIdDeed(Integer idDeed);

    List<Folio> findByYear(int year);

    List<Folio> findByStatus(String status);

    List<Folio> findByFkIdNotebook(Notebook notebook);

    List<Folio> findAllByIdFolioIn(List<Integer> ids);

    @Query("SELECT f FROM Folio f WHERE f.fkIdFolioType.isAuxiliary = true AND f.fkIdDeed IS NULL")
    List<Folio> findFoliosAuxiliaresDisponibles();

    @Query("SELECT MAX(f.fkIdDeed.number) FROM Folio f "
            + "WHERE f.fkIdFolioType.isAuxiliary = true AND f.fkIdDeed IS NOT NULL")
    Optional<Integer> findMaxNumberDeedAuxiliary();

    @Query("SELECT MAX(f.fkIdDeed.number) FROM Folio f "
            + "WHERE f.fkIdNotaryPerson.idPerson = :idEscribano AND f.year = :anio "
            + "AND f.fkIdFolioType.isAuxiliary = :esAuxiliar AND f.fkIdDeed IS NOT NULL "
            + "AND (:idEscrituraExcluir IS NULL OR f.fkIdDeed.idDeed <> :idEscrituraExcluir)")
    Optional<Integer> findMaxNumberDeedByNotaryYearYType(
            @Param("idEscribano") Integer idNotary,
            @Param("anio") int year,
            @Param("esAuxiliar") boolean isAuxiliary,
            @Param("idEscrituraExcluir") Integer idDeedExcluir);

    @Query("SELECT COUNT(f) > 0 FROM Folio f "
            + "WHERE f.fkIdDeed.number = :numero AND f.fkIdNotaryPerson.idPerson = :idEscribano "
            + "AND f.year = :anio AND f.fkIdFolioType.isAuxiliary = :esAuxiliar AND f.fkIdDeed IS NOT NULL "
            + "AND (:idEscrituraExcluir IS NULL OR f.fkIdDeed.idDeed <> :idEscrituraExcluir)")
    boolean existsNumberDeedByNotaryYearYType(
            @Param("numero") int number,
            @Param("idEscribano") Integer idNotary,
            @Param("anio") int year,
            @Param("esAuxiliar") boolean isAuxiliary,
            @Param("idEscrituraExcluir") Integer idDeedExcluir);
}
