package com.licensis.notaire.adapter.out.persistence.testimony;

import com.licensis.notaire.application.port.out.testimony.TestimonyMovementRepositoryPort;
import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.repository.TestimonyMovementRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for testimony movements.
 */
@Component
public class TestimonyMovementPersistenceAdapter implements TestimonyMovementRepositoryPort {

    private final TestimonyMovementRepository testimonyMovementRepository;

    public TestimonyMovementPersistenceAdapter(TestimonyMovementRepository testimonyMovementRepository) {
        this.testimonyMovementRepository = testimonyMovementRepository;
    }

    @Override
    public List<TestimonyMovement> findAll() {
        return testimonyMovementRepository.findAll();
    }

    @Override
    public Optional<TestimonyMovement> findById(Integer id) {
        return testimonyMovementRepository.findById(id);
    }

    @Override
    public TestimonyMovement save(TestimonyMovement entity) {
        return testimonyMovementRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        testimonyMovementRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return testimonyMovementRepository.existsById(id);
    }
}
