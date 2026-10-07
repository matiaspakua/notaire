# Design

## Context

`EditPaymentService` throws IllegalArgumentException for both 'not found' and 'amount <= 0'; the controller maps it to 404.

## Goals / Non-Goals

Goal: payment request validation. Non-goal: the remaining controllers of #655.

## Decisions

Keep the use-case checks as defence in depth; validate at the boundary with standard constraints so OpenAPI documents them.

## Riesgos / Trade-offs

oasdiff reports 4 ERR-level changes (required/exclusiveMinimum) although the runtime already rejected those values. Decision (Owner asked to resolve the red check): list exactly those 4 lines in `backend-api/openapi/accepted-breaking-changes.txt`, passed to oasdiff as `err-ignore` by `openapi-contract.yml` and `scripts/preflight.sh`. Rejected: hiding the constraints from the schema (the contract would lie about rules the API enforces) and dropping `fail-on: ERR` (any break would pass). `scripts/test_dast_contract_backup_assets.py` requires every entry to follow an issue comment.

## Testing Strategy

6 new parameterized cases + 1 partial-update case in `PaymentControllerTest`, observed failing first; one existing test input adjusted so it still exercises the use-case rejection path.

## Regression Strategy

All payment tests (unit, integration), Bruno `payments` folder against a local backend.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

Ships with the next backend image; no migration.

## Rollback Strategy

Revert the commit.
