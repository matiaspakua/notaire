package com.licensis.notaire.adapter.out.persistence.copy;

import com.licensis.notaire.application.port.out.copy.CopyRepositoryPort;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.repository.CopyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the copy slice.
 * The only place where {@link CopyRepository} is touched for this slice.
 */
@Component
public class CopyPersistenceAdapter implements CopyRepositoryPort {

    private final CopyRepository copyRepository;

    public CopyPersistenceAdapter(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @Override
    public List<Copy> findAll() {
        return copyRepository.findAll();
    }

    @Override
    public Optional<Copy> findById(Integer id) {
        return copyRepository.findById(id);
    }

    @Override
    public Copy save(Copy entity) {
        return copyRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        copyRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return copyRepository.existsById(id);
    }

    @Override
    public List<Copy> findByFkIdTestimony(Testimony testimony) {
        return copyRepository.findByFkIdTestimony(testimony);
    }

    @Override
    public List<Copy> findByFkIdTestimonyIdTestimony(Integer idTestimony) {
        return copyRepository.findByFkIdTestimonyIdTestimony(idTestimony);
    }

    @Override
    public List<Copy> findByFkIdPersonIdPerson(Integer idPerson) {
        return copyRepository.findByFkIdPersonIdPerson(idPerson);
    }

    @Override
    public List<Copy> findByNumber(int number) {
        return copyRepository.findByNumber(number);
    }
}
