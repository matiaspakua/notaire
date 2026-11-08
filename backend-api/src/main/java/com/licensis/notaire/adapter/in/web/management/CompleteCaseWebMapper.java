package com.licensis.notaire.adapter.in.web.management;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.licensis.notaire.application.port.out.management.ManagementOpsPort.CompleteCaseResult;
import com.licensis.notaire.dto.DtoManagementSummary;

/**
 * Maps complete-case write results to HTTP responses (#577).
 */
public class CompleteCaseWebMapper {

    public ResponseEntity<DtoManagementSummary> mapCreateResult(CompleteCaseResult result) {
        return switch (result) {
            case CompleteCaseResult.Created created -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(created.summary());
            case CompleteCaseResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case CompleteCaseResult.NotFound ignored -> ResponseEntity.notFound().build();
            case CompleteCaseResult.Updated ignored -> ResponseEntity.ok().build();
        };
    }

    public ResponseEntity<DtoManagementSummary> mapUpdateResult(CompleteCaseResult result) {
        return switch (result) {
            case CompleteCaseResult.Updated updated -> ResponseEntity.ok(updated.summary());
            case CompleteCaseResult.BadRequest ignored -> ResponseEntity.badRequest().build();
            case CompleteCaseResult.NotFound ignored -> ResponseEntity.notFound().build();
            case CompleteCaseResult.Created ignored -> ResponseEntity.status(HttpStatus.CREATED).build();
        };
    }
}
