package com.licensis.notaire.application.usecase.copy;

import com.licensis.notaire.application.port.out.copy.CopyRepositoryPort;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CopyService {

    private static final Logger logger = LoggerFactory.getLogger(CopyService.class);

    private final CopyRepositoryPort repository;
    private final TestimonyMovementRepository testimonyMovementRepository;

    public CopyService(CopyRepositoryPort repository,
                       TestimonyMovementRepository testimonyMovementRepository) {
        this.repository = repository;
        this.testimonyMovementRepository = testimonyMovementRepository;
    }

    @Transactional(readOnly = true)
    public List<Copy> findAll() {
        logger.debug("Finding all copies");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Copy> findById(Integer id) {
        logger.debug("Finding copy by id: {}", id);
        return repository.findById(id);
    }

    @Transactional
    public Copy save(Copy copy) {
        logger.debug("Saving copy: {}", copy);
        return repository.save(copy);
    }

    @Transactional
    public void deleteById(Integer id) {
        logger.debug("Deleting copy by id: {}", id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        logger.debug("Checking if copy exists by id: {}", id);
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean canCreateCopyForTestimony(Integer testimonyId) {
        logger.debug("Checking if can create copy for testimony id: {}", testimonyId);
        return !testimonyMovementRepository
                .existsByFkIdTestimonyIdTestimonyAndRegisteredTrue(testimonyId);
    }
}
