package com.licensis.notaire.application.port.in.substitution;

import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SaveResult;
import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionCommand;

/**
 * Write-side inbound port for substitutions (issue #1807).
 */
public interface ManageSubstitutionsUseCase {

    SaveResult create(SubstitutionCommand command);

    SaveResult update(Integer id, SubstitutionCommand command);

    DeleteResult delete(Integer id);
}
