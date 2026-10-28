> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-Wave-8 board review (#1963): top-10 critical open issues challenged into a
solvable Wave 9 close-set (security, CI, validation, OpenAPI, hex child).

## Goals / Non-Goals

**Goals:** Close the Wave 9 set (#1866, #1683, #254, #1684, #566, #655, #595, #1921, #1920, #1315).
**Non-Goals:** Close epic #1693; MFA #1640; product #774; full `jpa` retirement #576.

## Decisions

1. Ratchet Checkstyle/SpotBugs; Trivy fails on CRITICAL (#566).
2. Self-signed local HTTPS + production runbook (#254); no secrets in git.
3. Classify `IllegalArgumentException` messages: not-found → 404, else → 400 (#1921).
4. Enforce `role_modules` after JWT via filter (#1683).
5. Verify/close #1866 if already merged (#1875).

## Riesgos / Trade-offs

- [Module path map incomplete] → registry + tests for known API prefixes.
- [Ratchet hides absolute debt] → #710/#711 remain for zero-debt.
- [LAZY collections need open sessions] → cover with existing transactional services/tests.

## Testing Strategy

Unit + integration for grants, status mapping, fetch types, isolation script,
HTTPS property load, validation 400s; Bruno where contracts change.

## Regression Strategy

RBAC integration tests stay green; payment/budget/management existing tests updated only where status expectations change.

## Playwright Strategy

n/a unless frontend is required for grants UX; full E2E runs after merge as Gate 5 / goal.

## Deployment Strategy

Ships with the next product release after merge.

## Rollback Strategy

Revert the PR; CI ratchet baselines revert with it.

## Migration Plan

None (no Flyway expected).

## Open Questions

None.
