package com.licensis.notaire.adapter.out.persistence.procedure;

import com.licensis.notaire.application.port.out.procedure.ProcedureRepositoryPort;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ProcedureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter for the procedure slice.
 */
@Component
public class ProcedurePersistenceAdapter implements ProcedureRepositoryPort {

    private final ProcedureRepository procedureRepository;

    public ProcedurePersistenceAdapter(ProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    @Override
    public Page<Procedure> findAll(Pageable pageable) {
        return procedureRepository.findAll(pageable);
    }

    @Override
    public Optional<Procedure> findById(Integer id) {
        return procedureRepository.findById(id);
    }

    @Override
    public Procedure save(Procedure entity) {
        return procedureRepository.save(entity);
    }

    @Override
    public void deleteById(Integer id) {
        procedureRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Integer id) {
        return procedureRepository.existsById(id);
    }

    @Override
    public List<Procedure> findByFkIdProcedureTypeIdProcedureType(Integer idProcedureType) {
        return procedureRepository.findByFkIdProcedureTypeIdProcedureType(idProcedureType);
    }
}
