package com.licensis.notaire.application.usecase.substitution;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.substitution.QuerySubstitutionsUseCase;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionView;

/**
 * Read-side use case for substitutions — delegates to {@link SubstitutionOpsPort} (issue #1807).
 */
@Service
public class SubstitutionQueryUseCaseService implements QuerySubstitutionsUseCase {

    private final SubstitutionOpsPort ops;

    public SubstitutionQueryUseCaseService(SubstitutionOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubstitutionView> findAll() {
        return ops.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SubstitutionView> findById(Integer id) {
        return ops.findById(id);
    }
}
