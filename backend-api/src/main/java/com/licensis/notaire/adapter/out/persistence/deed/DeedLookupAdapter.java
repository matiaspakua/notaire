package com.licensis.notaire.adapter.out.persistence.deed;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.deed.DeedLookupPort;
import com.licensis.notaire.repository.DeedRepository;

@Component
public class DeedLookupAdapter implements DeedLookupPort {

    private final DeedRepository deeds;

    public DeedLookupAdapter(DeedRepository deeds) {
        this.deeds = deeds;
    }

    @Override
    public boolean existsById(Integer id) {
        return id != null && deeds.existsById(id);
    }
}
