package com.licensis.notaire.repository;

import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubstitutionRepository extends JpaRepository<Substitution, Integer> {

    @Query("SELECT s FROM Substitution s "
            + "JOIN FETCH s.fkIdSubstitute JOIN FETCH s.fkIdSubstituted WHERE s.idSubstitution = :id")
    Optional<Substitution> findByIdWithPersons(@Param("id") Integer id);

    @Query("SELECT s FROM Substitution s JOIN FETCH s.fkIdSubstitute JOIN FETCH s.fkIdSubstituted")
    List<Substitution> findAllWithPersons();

    List<Substitution> findByFkIdSubstitute(Person substitute);

    List<Substitution> findByFkIdSubstituteIdPerson(Integer substituteId);

    List<Substitution> findByFkIdSubstituted(Person substituted);

    List<Substitution> findByFkIdSubstitutedIdPerson(Integer substitutedId);

    @Query("SELECT s FROM Substitution s WHERE s.dateStart <= :date AND s.dateEnd >= :date")
    List<Substitution> findByDateStartBeforeAndDateEndAfter(@Param("date") Date date);

    List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer substitutedId, Date dateStart, Date dateEnd);
}
