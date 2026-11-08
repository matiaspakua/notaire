package com.licensis.notaire.application.port.in.testimony;

import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.DeleteResult;
import com.licensis.notaire.application.port.out.testimony.TestimonyOpsPort.SaveResult;
import com.licensis.notaire.dto.DtoTestimony;

/**
 * Write-side inbound port for testimonies (issue #1783).
 */
public interface ManageTestimoniesUseCase {

    SaveResult create(DtoTestimony dto);

    SaveResult update(Integer id, DtoTestimony dto);

    DeleteResult delete(Integer id);

    DtoTestimony generate(Integer idDeed);

    DtoTestimony verify(Integer id, boolean flagged, String notes);
}
