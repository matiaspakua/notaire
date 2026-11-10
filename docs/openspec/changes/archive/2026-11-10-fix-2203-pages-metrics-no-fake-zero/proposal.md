# Do not publish 0% coverage when the JaCoCo snapshot is missing

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #2203 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/fix-2203-pages-metrics-no-fake-zero-fc7d` |
| Gate 1 status | passed |

## Objetivo

When `coverage-snapshot/coverage-snapshot.json` (or the frontend Vitest summary) is absent, `workspace/ci/generate-github-page-metrics.sh` still writes numeric `0` for coverage fields — either as a hard default or by copying a previously published fake zero from the live Pages file. The portfolio then shows 0% as if JaCoCo had measured it. Factory honesty requires skip ≠ pass and no fake metrics.

## What Changes

- Change the metrics generator so a missing JaCoCo snapshot / frontend coverage summary does **not** write `0` for those coverage fields; write JSON `null` (and keep `lastUpdated: "unknown"`) when no measured value is available.
- Reuse the live deployed coverage field only when the live snapshot is itself measured (`lastUpdated` is not `unknown`); never copy a poisoned `0` forward.
- Keep updating fields from a real present snapshot / summary.
- Lock the fallback with a workspace test that runs the shared script against fixtures (missing snapshot + missing/poisoned live → no zero; real snapshot → updates).
- Narrow TypeScript / UI consumers so `null` coverage numbers render as not measured (aligned with `coverageIsMeasured` / Results `n/a`), and refresh committed `github-page/public/metrics.json` via the same script path.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Public coverage percentages are published only from a real measurement artifact; absence must not become a numeric zero. | Issue #2203, CU76, CONSTITUTION.md §7 | Made explicit |
| Carrying forward live metrics is allowed only when that live coverage was measured (`lastUpdated` ≠ `unknown`). | Issue #2203 | New |
| Activity counts (commits, tests, …) remain counted from the repository and are not coverage. | docs/openspec/specs/github-page-metrics-test-counts/spec.md | Unchanged |

## Capabilities

### New Capabilities

- `github-page-metrics-coverage-honesty`: generator and committed metrics must not publish fabricated 0% coverage when measurement files are missing.

### Modified Capabilities

- `github-page-metrics-test-counts`: coverage object may hold JSON `null` for unmeasured percentage fields (schema honesty; Vitest vs E2E counts unchanged).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | deploy workflows already call the shared script; no workflow rewrite expected |
| `github-page` | yes | metrics types, loaders/UI honesty, committed `metrics.json`, tests |
| `workspace` | yes | `generate-github-page-metrics.sh` + new/updated test |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none (optional `LIVE_METRICS_URL` / `GH_REPO` already exist)
- Dependencies: none
- BREAKING: portfolio `metrics.json` coverage percentages may become `null` instead of `0` when unmeasured — intentional honesty; Pages UI must tolerate `null`

### Write set

- `workspace/ci/generate-github-page-metrics.sh`
- `workspace/tests/test_generate_github_page_metrics.py`
- `github-page/public/metrics.json`
- `github-page/lib/metrics.ts`
- `github-page/components/**`
- `github-page/app/**`
- `github-page/lib/i18n/**`
- `github-page/tests/**`
- `docs/openspec/changes/fix-2203-pages-metrics-no-fake-zero/**`
- `CHANGELOG.md`

### Architecture review

Follows existing shared-script pattern (#758 / #1982). No ADR; behavior is a honesty fix on the Pages metrics generator, not a new architectural boundary.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | `[Unreleased]` note that Pages metrics no longer publish fabricated 0% coverage when JaCoCo/Vitest snapshots are missing |
| `github-page/README.md` | One sentence on coverage `null` / `lastUpdated: unknown` meaning unmeasured |

## Out of Scope

- Raising the JaCoCo floor / inventing a coverage number when no report exists
- Computing real `e2eTests` / `apiEndpoints` percentages (#760)
- Product app (`frontend/`) UI changes
