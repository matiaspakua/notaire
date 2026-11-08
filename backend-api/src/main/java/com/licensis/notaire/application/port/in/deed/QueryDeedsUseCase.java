package com.licensis.notaire.application.port.in.deed;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.NotaryView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.PageResult;

/**
 * Read-side inbound port for deeds (issue #1811).
 */
public interface QueryDeedsUseCase {

    PageResult findAll(String status, int page, int size, String sort);

    Optional<DeedView> findById(Integer id);

    List<DeedView> searchByNumber(Integer number);

    List<NotaryView> findAvailableNotaries();
}
