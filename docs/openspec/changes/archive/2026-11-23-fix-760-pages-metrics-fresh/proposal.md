# Compute real e2eTests and apiEndpoints on the Pages metrics

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #760 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `fix/760_pages_metrics_fresh` |
| Gate 1 status | passed |

## Objetivo

In `github-page/public/metrics.json`, `coverage.e2eTests` and `coverage.apiEndpoints` carry forward the last deployed value instead of being computed per run. The published numbers can be stale and look authoritative. This change computes both values fresh each run from Playwright results and the Bruno report, and marks any fallback value as stale so it is visibly distinguished from current data.

## What Changes

- **`generate-github-page-metrics.sh`**: Add a `--json` mode to `generate_e2e_coverage_report.py` (or a small helper function) that extracts the Playwright test count (`stats.expected + stats.unexpected + stats.skipped + stats.flaky`) and the Bruno total requests count, then writes them as `e2eTests` and `apiEndpoints` in the coverage JSON. On failure (artifact missing or parse error), fall back to the last live value but set a `stale` flag (boolean) and record the `lastUpdated` date so the UI can display it as stale.
- **`generate_e2e_coverage_report.py`**: Add a `--json` CLI flag that outputs a compact JSON object `{"e2eTests": <int>, "apiEndpoints": <int>}` instead of the Markdown report. Reuses the same `_playwright_section` and `_bruno_section` parsing logic.
- **`github-page/lib/metrics-core.ts`**: Extend `CoverageSnapshot` with an optional `stale` field on `e2eTests` and `apiEndpoints` (or a top-level `staleFields: string[]`). Update `coverageIsMeasured` and display helpers to treat stale values distinctly.
- **`github-page/components/Coverage.tsx`**: Render a "stale" badge / indicator when a coverage value is marked stale, so visitors see the value is not from the current run.
- **Tests**: Update `test_generate_github_page_metrics.py` with scenarios for fresh e2e/API values and stale fallback. Update `test_generate_e2e_coverage_report.py` with the new `--json` flag output.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| `e2eTests` MUST be computed each run from Playwright results, not carried forward. | CU76 | New |
| `apiEndpoints` MUST be computed each run from the Bruno report, not carried forward. | CU76 | New |
| When computing fails, the last live value is kept as fallback but MUST be visibly marked stale. | CU76 | New |
| A stale fallback MUST NOT be presented as current — the UI distinguishes it with a flag or date. | CU76 | New |

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `github-page-metrics-coverage-honesty`: Add requirements that `e2eTests` and `apiEndpoints` are computed fresh each run and that fallback values are marked stale.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `frontend-swing` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |
| `github-page` (Next.js static site) | yes | Coverage component shows stale badge; types extended |
| `testing` (tools + tests) | yes | `generate_e2e_coverage_report.py` gains `--json` flag; tests updated |
| `workspace/ci` (scripts) | yes | `generate-github-page-metrics.sh` wires fresh e2e/API counts |

### Surface area

- Entities: none
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none

### Write set

- ``workspace/ci/generate-github-page-metrics.sh``
- ``workspace/tests/test_generate_github_page_metrics.py``
- ``testing/tools/generate_e2e_coverage_report.py``
- ``testing/tests/test_generate_e2e_coverage_report.py``
- ``github-page/lib/**``
- ``github-page/components/**``
- ``docs/openspec/changes/**``
- ``docs/openspec/specs/**``

### Architecture review

Follows existing architecture: the shared metrics generator (`generate-github-page-metrics.sh`) already calls external tools and merges their output into `metrics.json`. The `generate_e2e_coverage_report.py` script already parses Playwright and Bruno artifacts — we extend it with a `--json` output mode. The Coverage UI component already handles `null` values — we add a stale indicator. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | n/a — not user visible (internal metrics page improvement) |

## Out of Scope

- Computing other coverage fields fresh (backend JaCoCo, frontend Vitest) — already handled.
- Changing the GitHub Pages deployment workflow — the script is already shared (#758).
- Adding new Playwright or Bruno tests — this change only reports existing results.
