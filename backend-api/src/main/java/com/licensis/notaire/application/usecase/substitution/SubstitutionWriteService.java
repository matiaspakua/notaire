package com.licensis.notaire.application.usecase.substitution;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.substitution.ManageSubstitutionsUseCase;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionCommand;

/**
 * Write-side use case for substitutions — delegates to {@link SubstitutionOpsPort} (issue #1807).
 */
@Service
public class SubstitutionWriteService implements ManageSubstitutionsUseCase {

    private final SubstitutionOpsPort ops;

    public SubstitutionWriteService(SubstitutionOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(SubstitutionCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, SubstitutionCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
