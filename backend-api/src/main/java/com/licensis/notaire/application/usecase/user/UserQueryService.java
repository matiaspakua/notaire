package com.licensis.notaire.application.usecase.user;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.application.port.out.user.UserCatalogPort;
import com.licensis.notaire.domain.user.User;

@Service
@Transactional(readOnly = true)
public class UserQueryService implements QueryUsersUseCase {

    private final UserCatalogPort catalog;

    public UserQueryService(UserCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<User> findAll() {
        return catalog.findAll();
    }

    @Override
    public Optional<User> findById(int id) {
        return catalog.findById(id);
    }

    @Override
    public Optional<User> findByPersonId(int personId) {
        return catalog.findFirstByPersonId(personId);
    }
}
