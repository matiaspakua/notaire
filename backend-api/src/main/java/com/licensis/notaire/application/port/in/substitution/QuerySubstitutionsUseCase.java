package com.licensis.notaire.application.port.in.substitution;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.substitution.SubstitutionOpsPort.SubstitutionView;

/**
 * Read-side inbound port for substitutions (issue #1807).
 */
public interface QuerySubstitutionsUseCase {

    List<SubstitutionView> findAll();

    Optional<SubstitutionView> findById(Integer id);
}
