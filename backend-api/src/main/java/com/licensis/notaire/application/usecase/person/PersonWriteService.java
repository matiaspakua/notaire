package com.licensis.notaire.application.usecase.person;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.person.ManagePeopleUseCase;
import com.licensis.notaire.application.port.out.person.PersonOpsPort;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonCommand;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult;

/**
 * Write-side use case for people — delegates to {@link PersonOpsPort} (issue #1813).
 */
@Service
public class PersonWriteService implements ManagePeopleUseCase {

    private final PersonOpsPort ops;

    public PersonWriteService(PersonOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(PersonCommand command) {
        return ops.create(command);
    }

    @Override
    public SaveResult update(Integer id, PersonCommand command) {
        return ops.update(id, command);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }
}
