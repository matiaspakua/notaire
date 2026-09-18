package com.licensis.notaire.adapter.out.persistence.substitution;

import com.licensis.notaire.application.port.out.substitution.SubstitutionRepositoryPort;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.SubstitutionRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the substitution slice.
 * The only place where {@link SubstitutionRepository} is touched for this slice.
 */
@Component
public class SubstitutionPersistenceAdapter implements SubstitutionRepositoryPort {

    private final SubstitutionRepository substitutionRepository;

    public SubstitutionPersistenceAdapter(SubstitutionRepository substitutionRepository) {
        this.substitutionRepository = substitutionRepository;
    }

    @Override
    public List<Substitution> findAll() {
        return substitutionRepository.findAllWithPersons();
    }

    @Override
    public Optional<Substitution> findById(Integer id) {
        return substitutionRepository.findByIdWithPersons(id);
    }

    @Override
    public Substitution save(Substitution entity) {
        return substitutionRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        substitutionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return substitutionRepository.existsById(id);
    }

    @Override
    public List<Substitution> findByFkIdSubstitute(Person suplente) {
        return substitutionRepository.findByFkIdSubstitute(suplente);
    }

    @Override
    public List<Substitution> findByFkIdSubstituteIdPerson(Integer idSuplente) {
        return substitutionRepository.findByFkIdSubstituteIdPerson(idSuplente);
    }

    @Override
    public List<Substitution> findByFkIdSubstituted(Person suplantado) {
        return substitutionRepository.findByFkIdSubstituted(suplantado);
    }

    @Override
    public List<Substitution> findByFkIdSubstitutedIdPerson(Integer idSuplantado) {
        return substitutionRepository.findByFkIdSubstitutedIdPerson(idSuplantado);
    }

    @Override
    public List<Substitution> findByDateStartBeforeAndDateEndAfter(Date date) {
        return substitutionRepository.findByDateStartBeforeAndDateEndAfter(date);
    }

    @Override
    public List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer idSuplantado, Date dateStart, Date dateEnd) {
        return substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        idSuplantado, dateStart, dateEnd);
    }
}
