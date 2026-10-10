# Design

## Context

`DtoManagementSummary` carries idManagement, number, encabezado, dateStart, statusActual, procedureCount, notes.

## Goals / Non-Goals

Goal: rows that identify a case. Non-goals: a shared status-badge mapping (separate epic item).

## Decisions

The id moves under the number instead of its own column, so the table gains two columns but only one net; the header truncates at 18rem with the full text in `title`.

## Riesgos / Trade-offs

None: presentation only.

## Testing Strategy

TS-0116 failed first (no Carátula column header).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0116, TS-0011, TS-0028 and the chromium suite against the branch build.

## Playwright Strategy

TS-0116: a case created via API shows its number, header and today's start date under Número/Carátula/Inicio/Trámites, and no column is titled Tipo de Trámite.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
