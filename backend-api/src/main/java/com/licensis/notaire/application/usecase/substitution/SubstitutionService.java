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
    public List<Substitution> findByFkIdSubstitute(Person suplente) {
        logger.debug("Finding substitutions by substitute: {}", suplente);
        return repository.findByFkIdSubstitute(suplente);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstituteIdPerson(Integer idSuplente) {
        logger.debug("Finding substitutions by substitute id: {}", idSuplente);
        return repository.findByFkIdSubstituteIdPerson(idSuplente);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstituted(Person suplantado) {
        logger.debug("Finding substitutions by substituted: {}", suplantado);
        return repository.findByFkIdSubstituted(suplantado);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstitutedIdPerson(Integer idSuplantado) {
        logger.debug("Finding substitutions by substituted id: {}", idSuplantado);
        return repository.findByFkIdSubstitutedIdPerson(idSuplantado);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByDateStartBeforeAndDateEndAfter(Date date) {
        logger.debug("Finding substitutions by date: {}", date);
        return repository.findByDateStartBeforeAndDateEndAfter(date);
    }

    @Transactional(readOnly = true)
    public List<Substitution> findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
            Integer idSuplantado, Date dateStart, Date dateEnd) {
        logger.debug(
                "Finding substitutions by substituted id={} and date range start={} end={}",
                idSuplantado, dateStart, dateEnd);
        return repository.findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                idSuplantado, dateStart, dateEnd);
    }
}
