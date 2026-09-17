package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureTypeRepositoryPort;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the procedure type slice.
 */
@Component
public class ProcedureTypePersistenceAdapter implements ProcedureTypeRepositoryPort {

    private final ProcedureTypeRepository procedureTypeRepository;

    public ProcedureTypePersistenceAdapter(ProcedureTypeRepository procedureTypeRepository) {
        this.procedureTypeRepository = procedureTypeRepository;
    }

    @Override
    public List<ProcedureType> findAll() {
        return procedureTypeRepository.findAll();
    }

    @Override
    public Optional<ProcedureType> findById(Integer id) {
        return procedureTypeRepository.findById(id);
    }

    @Override
    public ProcedureType save(ProcedureType entity) {
        return procedureTypeRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        procedureTypeRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return procedureTypeRepository.existsById(id);
    }

    @Override
    public List<ProcedureType> findByNameContaining(String name) {
        return procedureTypeRepository.findByNameContaining(name);
    }
}
