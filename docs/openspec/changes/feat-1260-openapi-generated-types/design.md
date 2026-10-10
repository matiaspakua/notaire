# Design — OpenAPI generated TS types (#1260)

## Context

Hand-written DTOs in `frontend/src/types/index.ts` drift from `openapi.yaml`. #1260 / #1197 P0.5.

## Goals / Non-Goals

**Goals:** regenerate with one command; CI drift fail; migrate four high-traffic hook areas.  
**Non-Goals:** Full SDK/client generation; migrating every hook in one PR; multi-repo publish.

## Decisions

| Decision | Choice | Alternative | Why alt lost |
|----------|--------|-------------|--------------|
| Tool | openapi-typescript | openapi-generator | Lighter; types-only; Node-native |
| Output | `src/types/api.generated.ts` | Multiple files | Simple drift diff |
| Migration | Incremental + thin re-exports | Big-bang replace index.ts | Lower risk |
| Drift check | regenerate to temp + `diff -q` | checksum only | Catches content drift |

## Riesgos / Trade-offs

Large generated file in PRs — accept. Dual type systems briefly — finish AC hooks same PR.

## Reachability guard

`api.generated.ts` lists every OpenAPI path as a bare string. The #1250 static reachability
scanner treats bare path literals as consumers for every HTTP method, so generated maps are
excluded via `*.generated.ts` (see `contracts/tests/test_api_reachability.py`). Hand-written
hooks remain the only consumers that clear allowlist entries.

## Testing Strategy

| Scenario | Verification |
|----------|--------------|
| One-command regen | `npm run openapi:types` |
| Drift fails CI | Frontend CI step + local script |
| Renamed field breaks tsc | Vitest/tsc on migrated hooks |
| Migrated hooks | Existing hook tests + typecheck |

## Regression Strategy

`npm run typecheck`, Vitest, Playwright on touched UI paths.

## Playwright Strategy

No intentional UI change; run suite because frontend types/hooks change. Prefer existing
specs for gestiones/documentos/presupuestos/dashboard.

## Deployment Strategy

Frontend static types only; next deploy after merge.

## Rollback Strategy

Revert PR; restore hand-written imports.
