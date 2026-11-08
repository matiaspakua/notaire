package com.licensis.notaire.application.port.in.testimony;

import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult;
import com.licensis.notaire.application.usecase.testimony.TestimonyReentry;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Write-side inbound port for testimony movements (issue #1787).
 */
public interface ManageTestimonyMovementsUseCase {

    SaveResult create(DtoTestimonyMovement dto);

    SaveResult update(Integer id, DtoTestimonyMovement dto);

    DeleteResult delete(Integer id);

    DtoTestimonyMovement registerEntry(Integer idTestimony);

    DtoTestimonyMovement registerInscription(Integer idTestimony);

    DtoTestimonyMovement withdraw(Integer idTestimony, int cardNumber);

    DtoTestimonyMovement reenter(Integer idTestimony, TestimonyReentry reentry);
}
