package com.licensis.notaire.application.port.out.substitution;

import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for substitution persistence operations.
 */
public interface SubstitutionRepositoryPort {

    List<Substitution> findAll();

    Optional<Substitution> findById(Integer id);

    Substitution save(Substitution entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Substitution> findByFkIdSubstitute(Person suplente);

    List<Substitution> findByFkIdSubstituteIdPerson(Integer idSuplente);

    List<Substitution> findByFkIdSubstituted(Person suplantado);

    List<Substitution> findByFkIdSubstitutedIdPerson(Integer idSuplantado);

    List<Substitution> findByDateStartBeforeAndDateEndAfter(Date date);

    List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer idSuplantado, Date dateStart, Date dateEnd);
}
