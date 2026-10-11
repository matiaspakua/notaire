package com.licensis.notaire.application.usecase.substitution;

import com.licensis.notaire.application.port.out.substitution.SubstitutionRepositoryPort;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SubstitutionService {

    private static final Logger logger = LoggerFactory.getLogger(SubstitutionService.class);

    private final SubstitutionRepositoryPort repository;

    public SubstitutionService(SubstitutionRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Substitution> findAll() {
        logger.debug("Finding all substitutions");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Substitution> findById(Integer id) {
        logger.debug("Finding substitution by id: {}", id);
        return repository.findById(id);
    }

    @Transactional
    public Substitution save(Substitution substitution) {
        logger.debug("Saving substitution: {}", substitution);
        return repository.save(substitution);
    }

    @Transactional
    public void deleteById(Integer id) {
        logger.debug("Deleting substitution by id: {}", id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        logger.debug("Checking if substitution exists by id: {}", id);
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstitute(Person substitute) {
        logger.debug("Finding substitutions by substitute: {}", substitute);
        return repository.findByFkIdSubstitute(substitute);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstituteIdPerson(Integer substituteId) {
        logger.debug("Finding substitutions by substitute id: {}", substituteId);
        return repository.findByFkIdSubstituteIdPerson(substituteId);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstituted(Person substituted) {
        logger.debug("Finding substitutions by substituted: {}", substituted);
        return repository.findByFkIdSubstituted(substituted);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstitutedIdPerson(Integer substitutedId) {
        logger.debug("Finding substitutions by substituted id: {}", substitutedId);
        return repository.findByFkIdSubstitutedIdPerson(substitutedId);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByDateStartBeforeAndDateEndAfter(Date date) {
        logger.debug("Finding substitutions by date: {}", date);
        return repository.findByDateStartBeforeAndDateEndAfter(date);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer substitutedId, Date dateStart, Date dateEnd) {
        logger.debug(
                "Finding substitutions by substituted id={} and date range start={} end={}",
                substitutedId, dateStart, dateEnd);
        return repository.findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                substitutedId, dateStart, dateEnd);
    }
}
