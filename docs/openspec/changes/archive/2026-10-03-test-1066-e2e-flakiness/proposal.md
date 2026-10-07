# Harden Playwright E2E: arrange data, no fixed sleeps, honest skips

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1066 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-1066-e2e-flakiness-69d3` (create at implement time) |
| Gate 1 status | draft ready (internal); implement after #1145 (#1054) merges |

## Objetivo

Playwright suites pass without proving behavior: TS-0021 (lines 47, 60, 74) and
TS-0022 (line 31) call `test.skip()` at runtime when workflow/tramite rows are
missing; seven fixed `waitForTimeout` sleeps (including 2000 ms in TS-0040 and
TS-0043), dozens of `networkidle` waits, a 300 s default timeout, and CI
`retries: 2` hide flakiness. Intentional feature-gap skips (TS-0014/16/17/20)
lack open-issue citations. This change makes E2E arrange their own data, wait
on web-first assertions, and fail loudly when flaky.

## What Changes

- TS-0021 / TS-0022 arrange required workflow / tipo-tramite data via
  `frontend/tests/e2e/setup/api-helpers.ts` (existing `createWorkflow*` helpers)
  so tests never skip for empty tables.
- Remove production-suite `waitForTimeout` fixed sleeps; replace with
  Playwright web-first assertions (`expect(...).toBeVisible()`, URL, locator
  state). Bound `networkidle` usage; prefer load-state / locator waits.
- Every intentional `test.skip` for a known feature gap MUST cite an open
  GitHub issue in the skip reason (create tracking issues at implement if none
  exist for TS-0014/16/17/20 residuals).
- Reduce Playwright CI retries from 2 → 1; keep flaky evidence via
  `trace: on-first-retry` / failure artifacts; document how flaky reports are
  tracked (CI artifacts + optional flake annotation in mapping docs).
- Review the global 300000 ms timeout: keep only where demo/long tours need it
  (project-scoped or test-scoped), not as a blanket mask for slow waits.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Automated E2E must exercise real flows, not skip when seed data is absent | CU76 – Quality Assurance and Testing Infrastructure | Made explicit |
| Waits must be condition-based (UI/API ready), not fixed wall-clock sleeps | CU76 – Ciclo de Verificación / CI pipeline | Made explicit |
| Intentional skips are temporary debt and must link an open tracking issue | CU76; CONSTITUTION §7 (no silent skips) | New (document in CU76 / E2E mapping) |
| CI retry budget must not hide systemic flakiness (max 1 retry in CI) | CU76; audit-2026-09 #1066 AC | Made explicit |

## Capabilities

### New Capabilities

- `e2e-test-reliability`: Playwright suite reliability rules — self-arranging
  data, web-first waits, cited intentional skips, reduced CI retries.

### Modified Capabilities

- (none under `openspec/specs/` today cover Playwright reliability policy)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | Consumes existing REST for test seeding only |
| `frontend` | yes | Playwright specs, `playwright.config.ts`, e2e helpers / waits |
| `frontend-swing` | no | Removed / out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | maybe | Only if workflow must align retry/artifact settings with config; prefer config-only |

### Surface area

- Entities: none (test data created via existing APIs, cleaned by teardown)
- Endpoints: no production contract change; E2E uses existing `/api/v1/*` seed helpers
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none new

### Architecture review

Follows existing `setup/api-helpers.ts` seed pattern already used by TS-0011,
TS-0029, carpetas-de-tramite, etc. No ADR. Does not expand product features
behind intentional skips — only links them to issues or implements arrange
paths where AC requires no skip.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | Note E2E reliability rules: arrange data, no fixed sleeps, cited skips, retry budget |
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | Update TS-0021/22 skip status; list intentional skips → issue links; flake-tracking note |
| `docs/300-development/303-testing/TEST-PLAN.md` | Align reliability / anti-flake guidance with #1066 |
| `CHANGELOG.md` | n/a — not user-visible product behavior (engineering/test infra) |

## Out of Scope

- **#1145 / #1054** — API error toasts (must merge first; do not conflict)
- Implementing missing product UI for intentional gaps (TS-0014/16/17/20) —
  only cite open issues unless a skip can be removed by arranging data alone
- Rewriting the entire E2E suite or demo tours (TS-0071/TS-0090) beyond removing
  unjustified fixed sleeps that affect CI reliability
- **#1067** — DAST / contract / backup tests
- Changing backend APIs or Flyway schema
