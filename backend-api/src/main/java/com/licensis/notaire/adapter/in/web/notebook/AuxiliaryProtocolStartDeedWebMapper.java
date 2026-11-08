package com.licensis.notaire.adapter.in.web.notebook;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;
import com.licensis.notaire.domain.notebook.AuxiliaryProtocolStartBadRequestException;
import com.licensis.notaire.domain.notebook.AuxiliaryProtocolStartNotFoundException;

import org.springframework.stereotype.Component;

@Component
public class AuxiliaryProtocolStartDeedWebMapper {

    public ResponseEntity<AuxiliaryProtocolController.DeedResponse> map(StartResult result) {
        return switch (result) {
            case StartResult.Created created -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuxiliaryProtocolController.DeedResponse.from(created.view()));
            case StartResult.BadRequest bad -> throw new AuxiliaryProtocolStartBadRequestException(bad.message());
            case StartResult.NotFound notFound -> throw new AuxiliaryProtocolStartNotFoundException(notFound.message());
        };
    }
}
