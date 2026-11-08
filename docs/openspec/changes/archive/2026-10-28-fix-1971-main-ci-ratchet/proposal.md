# Restore a green `main` after #1966 (quality ratchets and generated API types)

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1971 |
| Use Case | CU-76 |
| Branch | `fix/1971_main_ci_ratchet_regressions` |
| Gate 1 status | passed |

## Objetivo

Since #1966 (`e6dd27b8f`), `CI - Build, Test & Security` and `Frontend CI` fail on every
push to `main`, and `Deploy GitHub Page` is skipped because it waits for a successful CI.
This change removes the regressions so the required checks and the Pages deploy run again.

## What Changes

- Checkstyle: record bodies written as `{}` become `{ }` in the controllers and
  `ApiModulePathRegistry` that #1966 touched (`WhitespaceAround`). Count 3173 → 3141, under the
  3157 floor.
- SpotBugs (1187 → 1180, under the 1183 floor), fixed in code, no suppressions:
  - `BudgetController.CatalogItemsRequest` copies its list (`List.copyOf`) — `EI_EXPOSE_REP`/`EI_EXPOSE_REP2`.
  - `SecurityAndCorsConfig` takes `RoleModuleAuthorizationFilter` as a parameter of the
    `apiSecurityFilterChain` bean method instead of storing it — `EI_EXPOSE_REP2`.
  - `ConceptController` / `DocumentTypeController` use `Objects.requireNonNullElse` — `BX_UNBOXING_IMMEDIATELY_REBOXED`.
  - `IllegalArgumentResponses.isMissingResource` uses `toLowerCase(Locale.ROOT)` — `DM_CONVERT_CASE`.
- `frontend/src/types/api.generated.ts` regenerated with `npm run openapi:types`.
- `frontend/package-lock.json`: dev-only transitive `zod` 4.4.3 → 4.6.7 (GHSA-mrhx-c7xc-m3w5).
- `backend-api/Dockerfile` and `Dockerfile.slim`: the runtime stage runs `apk upgrade --no-cache`. CI `Build Docker Image` (which never ran on `main` while the gates were red) failed its CRITICAL Trivy scan on Alpine `brotli-libs` 1.2.0-r1 (CVE-2026-111497, fixed in 1.2.1-r0).

No **BREAKING** change: the HTTP contract, the module-grant filter order and the defaults are unchanged.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Quality floors only shrink; a regression is fixed, never absorbed by raising the floor | #566, `workspace/ci/quality-ratchet-baseline.json` | Made explicit |
| The generated frontend API types always match `openapi.yaml` | #1260, CU-76 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — restores existing behavior (`skip_specs: true`).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Formatting and SpotBugs fixes in 11 classes; 3 test classes |
| `frontend` | yes | Regenerated `api.generated.ts`; lockfile `zod` bump |
| `frontend-swing` | no | — |
| `infra` / observability | yes | Backend runtime images run `apk upgrade` |
| CI/CD (`.github/workflows`) | no | Workflows unchanged; they pass again |

### Surface area

- Entities: none
- Endpoints: none changed (`POST /api/v1/budgets/{id}/items-from-catalog` keeps its body)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: `zod` (dev, transitive) lock bump

### Architecture review

Follows the existing architecture; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | `[Unreleased]` → Bug Fixes entry for #1971 |

## Out of Scope

- Tightening the floors to the new counts (3141 / 1180): needs a `ci.yml` edit; tracked in #710 / #711.
- `github-page/` content (redesign tracked in #1970).
