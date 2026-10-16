package com.licensis.notaire.application.port.in.person;

import java.util.List;
import java.util.Optional;

import com.licensis.notaire.application.port.out.person.PersonOpsPort.PageResult;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonView;

/**
 * Read-side inbound port for people (issue #1813).
 */
public interface QueryPeopleUseCase {

    PageResult findAll(int page, int size, String sort);

    Optional<PersonView> findById(Integer id);

    List<PersonView> search(
            String firstName,
            String lastName,
            String identificationNumber,
            Integer idIdentificationType,
            Boolean isClient);
}
