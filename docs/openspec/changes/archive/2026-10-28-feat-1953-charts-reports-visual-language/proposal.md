# Charts and reports visual language

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #1953 |
| Use Case | RNF-09 – Uso de colores en la GUI |
| Branch | `feat/1953_charts-reports-visual-language` |
| Gate 1 status | passed |

## Objetivo

Wave 6 of epic #1941. Reports download cards and home metric tiles still use
page-local markup. Align them on shared primitives and PageSection / async chrome
so a future chart library inherits the same token language (no chart package yet).

## What Changes

- Shared `ReportCard` and `MetricCard` under `components/shared/`.
- `/dashboard/reports` uses PageSection groups + LoadingState/ErrorState for catalog queries.
- Dashboard home stats use `MetricCard`.
- FRONTEND-DESIGN-SYSTEM + CHANGELOG + Vitest.

## Capabilities

### New Capabilities

- `ui-reports-metrics`: report download cards and home metric tiles.

## Impact

| Module | Touched |
|--------|---------|
| `frontend` | yes |
| `backend-api` | no |
