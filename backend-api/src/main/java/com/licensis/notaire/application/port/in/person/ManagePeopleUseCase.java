package com.licensis.notaire.application.port.in.person;

import com.licensis.notaire.application.port.out.person.PersonOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.PersonCommand;
import com.licensis.notaire.application.port.out.person.PersonOpsPort.SaveResult;

/**
 * Write-side inbound port for people (issue #1813).
 */
public interface ManagePeopleUseCase {

    SaveResult create(PersonCommand command);

    SaveResult update(Integer id, PersonCommand command);

    DeleteResult delete(Integer id);
}
