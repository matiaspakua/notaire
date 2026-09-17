package com.licensis.notaire.adapter.out.persistence.notebook;

import com.licensis.notaire.application.port.out.notebook.DeedOperationPort;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter for deed operations within notebook context.
 */
@Component
public class DeedOperationAdapter implements DeedOperationPort {

    private final DeedRepository deedRepository;

    public DeedOperationAdapter(DeedRepository deedRepository) {
        this.deedRepository = deedRepository;
    }

    @Override
    public Deed save(Deed entity) {
        return deedRepository.save(entity);
    }
}
