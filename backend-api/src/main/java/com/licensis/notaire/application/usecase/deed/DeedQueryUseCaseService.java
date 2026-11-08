package com.licensis.notaire.application.usecase.deed;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.deed.QueryDeedsUseCase;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.NotaryView;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.PageResult;

/**
 * Read-side use case for deeds — delegates to {@link DeedOpsPort} (issue #1811).
 */
@Service
public class DeedQueryUseCaseService implements QueryDeedsUseCase {

    private final DeedOpsPort ops;

    public DeedQueryUseCaseService(DeedOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(String status, int page, int size, String sort) {
        return ops.findAll(status, page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DeedView> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeedView> searchByNumber(Integer number) {
        return ops.searchByNumber(number);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaryView> findAvailableNotaries() {
        return ops.findAvailableNotaries();
    }
}
