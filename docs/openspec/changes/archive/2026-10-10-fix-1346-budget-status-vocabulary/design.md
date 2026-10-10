# Design

## Context

No backend enum existed; the column held "Pendiente" (1434), "BORRADOR" (102) on the dev DB, and API tests posted "Pending". The status search is an exact match on the stored text.

## Goals / Non-Goals

Goal: one vocabulary end to end. Non-goals: budget status transitions or who may change them.

## Decisions

Codes as stored (Owner default) rather than English codes, so V44 changes only the mixed-case rows. Writes accept any letter case (clients typing `Pendiente` keep working) but no English aliases on the API, so the schema enum is exact; V44 maps English spellings once. Unknown stored values are not guessed.

## Riesgos / Trade-offs

A third-party client sending another free-text status now gets 400 (accepted breaking change). Unknown legacy rows must be given a valid status before they can be saved again.

## Testing Strategy

Red first (d3379a3e): backend tests failed to compile (enum missing), Vitest failed (module missing), TS-0115 2 failed (no Pendiente option, blank edit status).

## Regression Strategy

`mvn test` (backend unit), `bash frontend/verify.sh`, Playwright TS-0115, TS-0113, TS-0010, presupuesto-plantilla, presupuesto-catalogo-items and the chromium suite against the branch build with the branch backend (V44 applied).

## Playwright Strategy

TS-0115: a pending budget is found with the Pendiente filter, shows the translated label and keeps it in the edit dialog; the filter offers every status.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
