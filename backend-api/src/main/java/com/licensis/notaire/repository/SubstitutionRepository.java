package com.licensis.notaire.repository;

import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubstitutionRepository extends JpaRepository<Substitution, Integer> {

    List<Substitution> findByFkIdSubstitute(Person suplente);

    List<Substitution> findByFkIdSubstituteIdPerson(Integer idSuplente);

    List<Substitution> findByFkIdSubstituted(Person suplantado);

    List<Substitution> findByFkIdSubstitutedIdPerson(Integer idSuplantado);

    @Query("SELECT s FROM Substitution s WHERE s.dateStart <= :fecha AND s.dateEnd >= :fecha")
    List<Substitution> findByDateStartBeforeAndDateEndAfter(@Param("fecha") Date date);

    List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer idSuplantado, Date dateStart, Date dateEnd);
}
