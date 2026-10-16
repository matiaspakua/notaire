package com.licensis.notaire.application.usecase.person;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.licensis.notaire.application.port.in.person.QueryPeopleUseCase;
import com.licensis.notaire.application.port.out.person.PersonOpsPort;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PageResult;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView;

/**
 * Read-side use case for people — delegates to {@link PersonOpsPort} (issue #1813).
 */
@Service
public class PersonQueryUseCaseService implements QueryPeopleUseCase {

    private final PersonOpsPort ops;

    public PersonQueryUseCaseService(PersonOpsPort ops) {
        this.ops = ops;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult findAll(int page, int size, String sort) {
        return ops.findAll(page, size, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonView> findById(Integer id) {
        return ops.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonView> search(
            String firstName,
            String lastName,
            String identificationNumber,
            Integer idIdentificationType,
            Boolean isClient) {
        return ops.search(firstName, lastName, identificationNumber, idIdentificationType, isClient);
    }
}
