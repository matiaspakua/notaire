package com.licensis.notaire.application.port.in.copy;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.copy.CopyOpsPort.CopyView;
import com.licensis.notaire.application.port.out.copy.CopyOpsPort.PageResult;

/**
 * Read-side inbound port for copies (issue #1779).
 */
public interface QueryCopiesUseCase {

    List<CopyView> findAll();

    PageResult findPage(int page, int size, String sort);

    Optional<CopyView> findById(Integer id);
}
