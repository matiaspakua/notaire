package com.licensis.notaire.application.usecase.testimony;

import org.springframework.stereotype.Service;

import com.licensis.notaire.application.port.in.testimony.ManageTestimonyMovementsUseCase;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.testimony.TestimonyMovementOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoTestimonyMovement;

/**
 * Write-side use case for testimony movements — delegates to {@link TestimonyMovementOpsPort}
 * (issue #1787). Transactions for caught CRUD error paths live on the outbound adapter.
 */
@Service
public class TestimonyMovementWriteService implements ManageTestimonyMovementsUseCase {

    private final TestimonyMovementOpsPort ops;

    public TestimonyMovementWriteService(TestimonyMovementOpsPort ops) {
        this.ops = ops;
    }

    @Override
    public SaveResult create(DtoTestimonyMovement dto) {
        return ops.create(dto);
    }

    @Override
    public SaveResult update(Integer id, DtoTestimonyMovement dto) {
        return ops.update(id, dto);
    }

    @Override
    public DeleteResult delete(Integer id) {
        return ops.delete(id);
    }

    @Override
    public DtoTestimonyMovement registerEntry(Integer idTestimony) {
        return ops.registerEntry(idTestimony);
    }

    @Override
    public DtoTestimonyMovement registerInscription(Integer idTestimony) {
        return ops.registerInscription(idTestimony);
    }

    @Override
    public DtoTestimonyMovement withdraw(Integer idTestimony, int cardNumber) {
        return ops.withdraw(idTestimony, cardNumber);
    }

    @Override
    public DtoTestimonyMovement reenter(Integer idTestimony, TestimonyReentry reentry) {
        return ops.reenter(idTestimony, reentry);
    }
}
