package com.licensis.notaire.adapter.out.persistence.testimony;

import com.licensis.notaire.application.port.out.testimony.TestimonyRepositoryPort;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.TestimonyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the testimony slice.
 */
@Component
public class TestimonyPersistenceAdapter implements TestimonyRepositoryPort {

    private final TestimonyRepository testimonyRepository;

    public TestimonyPersistenceAdapter(TestimonyRepository testimonyRepository) {
        this.testimonyRepository = testimonyRepository;
    }

    @Override
    public List<Testimony> findAll() {
        return testimonyRepository.findAll();
    }

    @Override
    public Optional<Testimony> findById(Integer id) {
        return testimonyRepository.findById(id);
    }

    @Override
    public Testimony save(Testimony entity) {
        return testimonyRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        testimonyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return testimonyRepository.existsById(id);
    }
}
