package com.licensis.notaire.application.usecase.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.user.QueryRolesUseCase;
import com.licensis.notaire.application.port.out.user.RoleCatalogPort;
import com.licensis.notaire.domain.user.Role;

@Service
@Transactional(readOnly = true)
public class RoleQueryService implements QueryRolesUseCase {

    private final RoleCatalogPort catalog;

    public RoleQueryService(RoleCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Role> findAll() {
        return catalog.findAll();
    }

    @Override
    public Optional<Role> findById(int id) {
        return catalog.findById(id);
    }
}
